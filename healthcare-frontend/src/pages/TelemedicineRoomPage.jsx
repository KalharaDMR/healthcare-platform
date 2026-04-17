import React, { useEffect, useMemo, useRef, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import AgoraRTC from 'agora-rtc-sdk-ng';
import Button from '../components/common/Button';
import Card from '../components/common/Card';
import { useAuth } from '../context/AuthContext';
import { telemedicineApi } from '../api/telemedicineApi';

const AGORA_APP_ID = process.env.REACT_APP_AGORA_APP_ID;

export default function TelemedicineRoomPage() {
  const { appointmentId } = useParams();
  const navigate = useNavigate();
  const { user, isDoctor } = useAuth();

  const [joining, setJoining] = useState(true);
  const [inCall, setInCall] = useState(false);
  const [micMuted, setMicMuted] = useState(false);
  const [cameraOff, setCameraOff] = useState(false);
  const [channelName, setChannelName] = useState('');
  const [remoteUsers, setRemoteUsers] = useState([]);

  const localPlayerRef = useRef(null);
  const clientRef = useRef(null);
  const localTracksRef = useRef({ micTrack: null, cameraTrack: null });

  const backPath = useMemo(
    () => (isDoctor ? '/doctor/appointments' : '/patient/appointments'),
    [isDoctor],
  );

  useEffect(() => {
    if (!inCall) return;
    const cameraTrack = localTracksRef.current.cameraTrack;
    if (!cameraTrack) return;
    cameraTrack.play('local-player');
  }, [inCall]);

  useEffect(() => {
    if (!AGORA_APP_ID) {
      toast.error('Agora app ID is missing. Set REACT_APP_AGORA_APP_ID in frontend env.');
      navigate(backPath, { replace: true });
      return undefined;
    }

    let mounted = true;

    const handleUserPublished = async (userItem, mediaType) => {
      const client = clientRef.current;
      if (!client) return;
      await client.subscribe(userItem, mediaType);

      if (mediaType === 'video') {
        const containerId = `remote-player-${userItem.uid}`;
        setRemoteUsers((prev) => {
          if (prev.find((u) => u.uid === userItem.uid)) return prev;
          return [...prev, userItem];
        });

        setTimeout(() => {
          userItem.videoTrack?.play(containerId);
        }, 0);
      }

      if (mediaType === 'audio') {
        userItem.audioTrack?.play();
      }
    };

    const handleUserUnpublished = (userItem) => {
      setRemoteUsers((prev) => prev.filter((u) => u.uid !== userItem.uid));
    };

    const joinCall = async () => {
      setJoining(true);
      try {
        if (isDoctor) {
          try {
            await telemedicineApi.createSession(Number(appointmentId));
          } catch {
            // Session may already exist; joining still works.
          }
        }

        const joinRes = await telemedicineApi.joinSession(Number(appointmentId));
        if (!mounted) throw new Error('JOIN_ABORTED');
        const roomChannel = joinRes.data?.channelName;
        const roomToken = joinRes.data?.token;

        if (!roomChannel || !roomToken) {
          throw new Error('Invalid telemedicine join response');
        }

        const client = AgoraRTC.createClient({ mode: 'rtc', codec: 'vp8' });
        clientRef.current = client;
        client.on('user-published', handleUserPublished);
        client.on('user-unpublished', handleUserUnpublished);
        client.on('user-left', handleUserUnpublished);

        // Let Agora assign a numeric UID to avoid duplicate-string UID conflicts in dev StrictMode.
        await client.join(AGORA_APP_ID, roomChannel, roomToken, null);
        if (!mounted) throw new Error('JOIN_ABORTED');

        const [micTrack, cameraTrack] = await AgoraRTC.createMicrophoneAndCameraTracks();
        if (!mounted) {
          micTrack.stop();
          micTrack.close();
          cameraTrack.stop();
          cameraTrack.close();
          throw new Error('JOIN_ABORTED');
        }
        localTracksRef.current = { micTrack, cameraTrack };
        await client.publish([micTrack, cameraTrack]);

        if (mounted) {
          setChannelName(roomChannel);
          setInCall(true);
        }
      } catch (err) {
        console.error(err);
        if (err?.message !== 'JOIN_ABORTED') {
          toast.error(err?.response?.data?.message || err.message || 'Unable to join consultation');
          navigate(backPath, { replace: true });
        }
      } finally {
        if (mounted) setJoining(false);
      }
    };

    joinCall();

    return () => {
      mounted = false;
      cleanupCallResources();
    };
  }, [appointmentId, backPath, isDoctor, navigate, user?.username]);

  const cleanupCallResources = async () => {
    const { micTrack, cameraTrack } = localTracksRef.current;

    if (micTrack) {
      micTrack.stop();
      micTrack.close();
    }
    if (cameraTrack) {
      cameraTrack.stop();
      cameraTrack.close();
    }

    localTracksRef.current = { micTrack: null, cameraTrack: null };

    if (clientRef.current) {
      await clientRef.current.leave();
      clientRef.current.removeAllListeners();
      clientRef.current = null;
    }
  };

  const handleToggleMic = async () => {
    const micTrack = localTracksRef.current.micTrack;
    if (!micTrack) return;
    const nextMuted = !micMuted;
    await micTrack.setMuted(nextMuted);
    setMicMuted(nextMuted);
  };

  const handleToggleCamera = async () => {
    const cameraTrack = localTracksRef.current.cameraTrack;
    if (!cameraTrack) return;
    const nextOff = !cameraOff;
    await cameraTrack.setMuted(nextOff);
    setCameraOff(nextOff);
  };

  const handleLeave = async () => {
    await cleanupCallResources();
    setInCall(false);
    navigate(backPath, { replace: true });
  };

  const handleEndForAll = async () => {
    try {
      await telemedicineApi.endSession(Number(appointmentId));
      toast.success('Session ended');
    } catch (err) {
      toast.error(err?.response?.data?.message || 'Unable to end session');
    } finally {
      await handleLeave();
    }
  };

  return (
    <div style={{ maxWidth: 1080, animation: 'fadeIn 0.3s ease' }}>
      <div style={{ marginBottom: 20 }}>
        <h1 style={{ fontSize: '1.5rem', fontWeight: 700 }}>Telemedicine consultation</h1>
        <p style={{ color: 'var(--text-secondary)', marginTop: 6 }}>
          Appointment #{appointmentId}{channelName ? ` · Channel: ${channelName}` : ''}
        </p>
      </div>

      <Card title="Consultation room">
        <div style={{ display: 'grid', gap: 14 }}>
          {joining && (
            <p style={{ margin: 0, color: 'var(--text-secondary)' }}>Joining consultation...</p>
          )}
          <div
            style={{
              display: 'grid',
              gap: 12,
              gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
            }}
          >
            <div>
              <div style={{ marginBottom: 8, fontWeight: 600 }}>You</div>
              <div
                id="local-player"
                ref={localPlayerRef}
                style={{ width: '100%', height: 220, borderRadius: 10, background: '#0f172a', overflow: 'hidden' }}
              />
            </div>

            <div>
              <div style={{ marginBottom: 8, fontWeight: 600 }}>Remote participant</div>
              {remoteUsers.length > 0 ? (
                <div style={{ display: 'grid', gap: 10 }}>
                  {remoteUsers.map((remoteUser) => (
                    <div
                      key={remoteUser.uid}
                      id={`remote-player-${remoteUser.uid}`}
                      style={{ width: '100%', height: 220, borderRadius: 10, background: '#111827', overflow: 'hidden' }}
                    />
                  ))}
                </div>
              ) : (
                <div
                  style={{
                    width: '100%',
                    height: 220,
                    borderRadius: 10,
                    background: '#f4f7fb',
                    border: '1px dashed var(--border)',
                    display: 'grid',
                    placeItems: 'center',
                    color: 'var(--text-secondary)',
                    fontSize: '0.9rem',
                  }}
                >
                  Waiting for the other participant to join...
                </div>
              )}
            </div>
          </div>

          <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            <Button variant="secondary" onClick={handleToggleMic} disabled={!inCall}>
              {micMuted ? 'Unmute mic' : 'Mute mic'}
            </Button>
            <Button variant="secondary" onClick={handleToggleCamera} disabled={!inCall}>
              {cameraOff ? 'Turn camera on' : 'Turn camera off'}
            </Button>
            <Button variant="ghost" onClick={handleLeave}>Leave</Button>
            {isDoctor && (
              <Button variant="danger" onClick={handleEndForAll}>End session</Button>
            )}
          </div>
        </div>
      </Card>
    </div>
  );
}
