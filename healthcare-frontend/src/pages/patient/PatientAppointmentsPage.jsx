import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { Video } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import { appointmentApi } from '../../api/appointmentApi';
import { paymentApi } from '../../api/paymentApi';

export default function PatientAppointmentsPage() {
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(false);

  const getVideoJoinState = (appointment) => {
    if (!appointment?.isVideoConferencingAppointment) {
      return { canJoin: false, reason: 'Not a video appointment' };
    }

    if (appointment?.status === 'CANCELLED') {
      return { canJoin: false, reason: 'Unavailable: appointment is cancelled' };
    }

    if (appointment?.status === 'COMPLETED') {
      return { canJoin: false, reason: 'Unavailable: appointment is completed' };
    }

    return { canJoin: true, reason: '' };
  };

  const loadAppointments = async () => {
    setLoading(true);
    try {
      const res = await appointmentApi.getMyAppointments();
      setAppointments(res.data || []);
    } catch {
      toast.error('Failed to load appointments');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAppointments();
  }, []);

  const handleCancelAppointment = async (appointment) => {
    try {
      await paymentApi.patientRefund({ slotId: appointment.slotId, appointmentId: appointment.id });
      toast.success('Refund requested. Status updates after payment webhook.');
      loadAppointments();
    } catch (err) {
      toast.error(err.response?.data || 'Unable to cancel appointment');
    }
  };

  const handleJoinVideo = async (appointmentId) => {
    navigate(`/patient/consultation/${appointmentId}`);
  };

  return (
    <div style={{ maxWidth: 900, animation: 'fadeIn 0.3s ease' }}>
      <div style={{ marginBottom: 24 }}>
        <h1 style={{ fontSize: '1.65rem', fontWeight: 700 }}>My appointments</h1>
        <p style={{ color: 'var(--text-secondary)', marginTop: 6 }}>
          Manage visits, join telemedicine, or cancel and request a refund.
        </p>
      </div>

      <Card title="Upcoming & past" action={<Button variant="ghost" onClick={loadAppointments} loading={loading}>Refresh</Button>}>
        {appointments.length === 0 && !loading ? (
          <p style={{ color: 'var(--text-secondary)' }}>No appointments yet. Book a slot from Find doctors.</p>
        ) : (
          <div style={{ display: 'grid', gap: 12 }}>
            {appointments.map((a) => (
              (() => {
                const videoState = getVideoJoinState(a);
                return (
              <div
                key={a.id}
                style={{
                  border: '1px solid var(--border)',
                  borderRadius: 10,
                  padding: 16,
                  display: 'grid',
                  gap: 10,
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 8 }}>
                  <div style={{ fontWeight: 600 }}>Appointment #{a.id} · Dr. {a.doctorUsername}</div>
                  <Badge label={a.status} />
                </div>
                <div style={{ color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
                  {a.appointmentDate} · {a.startTime} – {a.endTime} · {a.hospital}
                </div>
                <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                  <Button variant="danger" onClick={() => handleCancelAppointment(a)}>Cancel + refund</Button>
                  <Button
                    variant="secondary"
                    onClick={() => handleJoinVideo(a.id)}
                    icon={<Video size={16} />}
                    disabled={!videoState.canJoin}
                  >
                    Join video
                  </Button>
                </div>
                {!videoState.canJoin && (
                  <p style={{ margin: 0, fontSize: '0.8125rem', color: 'var(--text-muted)' }}>
                    {videoState.reason}
                  </p>
                )}
              </div>
                );
              })()
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}
