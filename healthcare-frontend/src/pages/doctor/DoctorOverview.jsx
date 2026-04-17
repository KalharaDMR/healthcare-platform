import React, { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Calendar, CalendarClock, Stethoscope, User, Video } from 'lucide-react';
import toast from 'react-hot-toast';
import { useAuth } from '../../context/AuthContext';
import { useExchangeRate } from '../../context/ExchangeRateContext';
import { doctorApi } from '../../api/doctorApi';
import { appointmentApi } from '../../api/appointmentApi';

export default function DoctorOverview() {
  const { user } = useAuth();
  const { usdToLkrRate, loading: fxLoading, usingFallback, refresh: refreshFx } = useExchangeRate();
  const [slots, setSlots] = useState([]);
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const [slotRes, appointmentRes] = await Promise.all([
          doctorApi.getMyAvailability(),
          appointmentApi.getDoctorAppointments(),
        ]);
        if (!cancelled) {
          setSlots(slotRes.data || []);
          setAppointments(appointmentRes.data || []);
        }
      } catch {
        if (!cancelled) toast.error('Failed to load overview');
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => { cancelled = true; };
  }, []);

  const stats = useMemo(
    () => ({
      slots: slots.length,
      pending: appointments.filter((a) => a.status === 'BOOKED').length,
      video: appointments.filter((a) => a.isVideoConferencingAppointment).length,
    }),
    [slots, appointments]
  );

  const tiles = [
    {
      to: '/doctor/profile',
      icon: <User size={26} color="var(--primary)" />,
      title: 'Profile',
      desc: 'Update your name, contact, specialization, and license.',
      accent: 'rgba(10,110,97,0.12)',
    },
    {
      to: '/doctor/availability',
      icon: <CalendarClock size={26} color="#7C3AED" />,
      title: 'Availability',
      desc: 'Create time slots, set LKR fees, and manage your calendar.',
      accent: '#EDE9FE',
    },
    {
      to: '/doctor/appointments',
      icon: <Calendar size={26} color="#1D4ED8" />,
      title: 'Appointments',
      desc: 'Complete visits, telemedicine, and patient refunds.',
      accent: '#DBEAFE',
    },
  ];

  return (
    <div style={{ maxWidth: 960, animation: 'fadeIn 0.35s ease' }}>
      <div style={{ marginBottom: 28 }}>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 700 }}>
          Welcome, Dr. {user?.username}
        </h1>
        <p style={{ color: 'var(--text-secondary)', marginTop: 6, maxWidth: 560 }}>
          Use the sidebar to move between areas—everything is organized so you can focus on one task at a time.
        </p>
        <p style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', marginTop: 10 }}>
          {fxLoading ? 'Loading USD→LKR rate…' : (
            <>
              1 USD ≈ {usdToLkrRate.toFixed(2)} LKR
              {usingFallback ? ' (offline fallback)' : ' (live rate)'}
              {' · '}
              <button
                type="button"
                onClick={() => refreshFx(true)}
                style={{
                  background: 'none',
                  border: 'none',
                  color: 'var(--primary)',
                  cursor: 'pointer',
                  font: 'inherit',
                  textDecoration: 'underline',
                }}
              >
                Refresh rate
              </button>
            </>
          )}
        </p>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 16, marginBottom: 32 }}>
        {[
          { icon: <CalendarClock size={22} />, label: 'Availability slots', value: loading ? '—' : stats.slots, color: 'var(--primary)', bg: 'rgba(10,110,97,0.1)' },
          { icon: <Stethoscope size={22} />, label: 'Booked (pending)', value: loading ? '—' : stats.pending, color: '#7C3AED', bg: '#EDE9FE' },
          { icon: <Video size={22} />, label: 'Video visits', value: loading ? '—' : stats.video, color: '#1D4ED8', bg: '#DBEAFE' },
        ].map((card) => (
          <div
            key={card.label}
            style={{
              background: 'var(--bg-card)',
              borderRadius: 'var(--radius-md)',
              padding: 20,
              border: '1px solid var(--border)',
              boxShadow: 'var(--shadow-sm)',
              display: 'flex',
              alignItems: 'center',
              gap: 14,
            }}
          >
            <div style={{ width: 48, height: 48, borderRadius: 14, background: card.bg, display: 'flex', alignItems: 'center', justifyContent: 'center', color: card.color }}>
              {card.icon}
            </div>
            <div>
              <div style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', fontWeight: 500 }}>{card.label}</div>
              <div style={{ fontSize: '1.5rem', fontWeight: 700, fontFamily: 'Sora,sans-serif' }}>{card.value}</div>
            </div>
          </div>
        ))}
      </div>

      <h2 style={{ fontSize: '1rem', fontWeight: 700, marginBottom: 14, color: 'var(--text-secondary)' }}>Where to next</h2>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))', gap: 16 }}>
        {tiles.map((t) => (
          <Link key={t.to} to={t.to} style={{ textDecoration: 'none', color: 'inherit' }}>
            <div
              style={{
                borderRadius: 'var(--radius-md)',
                padding: 22,
                border: '1px solid var(--border)',
                background: 'var(--bg-card)',
                height: '100%',
                transition: 'transform 0.2s, box-shadow 0.2s, border-color 0.2s',
              }}
              onMouseEnter={(e) => {
                e.currentTarget.style.boxShadow = 'var(--shadow-md)';
                e.currentTarget.style.transform = 'translateY(-2px)';
                e.currentTarget.style.borderColor = 'var(--primary)';
              }}
              onMouseLeave={(e) => {
                e.currentTarget.style.boxShadow = '';
                e.currentTarget.style.transform = '';
                e.currentTarget.style.borderColor = 'var(--border)';
              }}
            >
              <div style={{ width: 48, height: 48, borderRadius: 12, background: t.accent, display: 'flex', alignItems: 'center', justifyContent: 'center', marginBottom: 12 }}>
                {t.icon}
              </div>
              <div style={{ fontWeight: 700, fontSize: '1.05rem', marginBottom: 6 }}>{t.title}</div>
              <p style={{ margin: 0, fontSize: '0.9rem', color: 'var(--text-secondary)', lineHeight: 1.45 }}>{t.desc}</p>
              <div style={{ marginTop: 14, display: 'inline-flex', alignItems: 'center', gap: 6, color: 'var(--primary)', fontWeight: 600, fontSize: '0.875rem' }}>
                Open <ArrowRight size={16} />
              </div>
            </div>
          </Link>
        ))}
      </div>
    </div>
  );
}
