import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { Video } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Badge from '../../components/common/Badge';
import { appointmentApi } from '../../api/appointmentApi';
import { telemedicineApi } from '../../api/telemedicineApi';
import { paymentApi } from '../../api/paymentApi';

export default function DoctorAppointmentsPage() {
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  const loadAppointments = async () => {
    setLoading(true);
    try {
      const res = await appointmentApi.getDoctorAppointments();
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

  const updateAppointmentStatus = async (appointmentId, status) => {
    try {
      await appointmentApi.updateDoctorAppointmentStatus(appointmentId, status);
      toast.success(`Appointment ${status}`);
      loadAppointments();
    } catch {
      toast.error('Status update failed');
    }
  };

  const startSession = async (appointmentId) => {
    try {
      const res = await telemedicineApi.createSession(appointmentId);
      toast.success(`Session created: ${res.data?.channelName}`);
    } catch {
      toast.error('Unable to create telemedicine session');
    }
  };

  const joinSession = async (appointmentId) => {
    navigate(`/doctor/consultation/${appointmentId}`);
  };

  const refundAndCancel = async (appointment) => {
    try {
      await paymentApi.doctorRefund({ appointmentId: appointment.id, slotId: appointment.slotId });
      toast.success('Doctor refund initiated');
      loadAppointments();
    } catch (err) {
      toast.error(err.response?.data || 'Refund request failed');
    }
  };

  return (
    <div style={{ maxWidth: 960, animation: 'fadeIn 0.3s ease' }}>
      <div style={{ marginBottom: 24 }}>
        <h1 style={{ fontSize: '1.65rem', fontWeight: 700 }}>Appointments</h1>
        <p style={{ color: 'var(--text-secondary)', marginTop: 6, maxWidth: 560 }}>
          Update visit status, run video visits, or process refunds when needed.
        </p>
      </div>

      <Card title="Your schedule" action={<Button variant="ghost" onClick={loadAppointments} loading={loading}>Refresh</Button>}>
        {appointments.length === 0 && !loading ? (
          <p style={{ color: 'var(--text-secondary)', margin: 0 }}>No appointments yet.</p>
        ) : (
          <div style={{ display: 'grid', gap: 12 }}>
            {appointments.map((a) => (
              <div key={a.id} style={{ border: '1px solid var(--border)', borderRadius: 10, padding: 16, display: 'grid', gap: 12 }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 8 }}>
                  <div style={{ fontWeight: 600 }}>#{a.id} · {a.patientUsername}</div>
                  <Badge label={a.status} />
                </div>
                <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
                  {a.appointmentDate} · {a.startTime} – {a.endTime} · {a.hospital} · {a.isVideoConferencingAppointment ? 'Video' : 'In-person'}
                </div>
                <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                  <Button onClick={() => updateAppointmentStatus(a.id, 'COMPLETED')}>Mark completed</Button>
                  <Button variant="ghost" onClick={() => navigate(`/doctor/clinical?patient=${encodeURIComponent(a.patientUsername)}`)}>
                    Clinical data
                  </Button>
                  <Button variant="secondary" onClick={() => startSession(a.id)}>Create session</Button>
                  <Button variant="secondary" onClick={() => joinSession(a.id)} icon={<Video size={16} />}>Join session</Button>
                  <Button variant="danger" onClick={() => refundAndCancel(a)}>Cancel + refund</Button>
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}
