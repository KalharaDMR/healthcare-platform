import React, { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ArrowRight, Calendar, Sparkles, Stethoscope, Video } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useExchangeRate } from '../../context/ExchangeRateContext';
import Button from '../../components/common/Button';
import { appointmentApi } from '../../api/appointmentApi';

export default function PatientOverview() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const { usdToLkrRate, loading: fxLoading, usingFallback, refresh: refreshFx } = useExchangeRate();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const res = await appointmentApi.getMyAppointments();
        if (!cancelled) setAppointments(res.data || []);
      } catch {
        if (!cancelled) setAppointments([]);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => { cancelled = true; };
  }, []);

  const stats = useMemo(() => ({
    appointments: appointments.length,
    video: appointments.filter((a) => a.isVideoConferencingAppointment).length,
  }), [appointments]);

  return (
    <div style={{ maxWidth: 960, animation: 'fadeIn 0.3s ease' }}>
      <div style={{ marginBottom: 28 }}>
        <h1 style={{ fontSize: '1.75rem', fontWeight: 700 }}>
          Welcome, {user?.username}
        </h1>
        <p style={{ color: 'var(--text-secondary)', marginTop: 6, maxWidth: 560 }}>
          Browse verified doctors, book a slot, manage visits, and optionally use the AI symptom assistant—all from the sidebar.
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

      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 18, marginBottom: 28 }}>
        {[
          { icon: <Calendar size={22} />, label: 'My appointments', value: loading ? '—' : stats.appointments, color: 'var(--primary)', bg: 'rgba(10,110,97,0.1)' },
          { icon: <Video size={22} />, label: 'Video visits', value: loading ? '—' : stats.video, color: '#1D4ED8', bg: '#DBEAFE' },
        ].map((card) => (
          <div
            key={card.label}
            style={{
              background: 'var(--bg-card)',
              borderRadius: 'var(--radius-md)',
              padding: 22,
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

      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(260px, 1fr))',
          gap: 18,
        }}
      >
        <Link to="/patient/doctors" style={{ textDecoration: 'none', color: 'inherit' }}>
          <div
            style={{
              borderRadius: 'var(--radius-md)',
              padding: 24,
              border: '1px solid var(--border)',
              background: 'linear-gradient(135deg, rgba(10,110,97,0.08) 0%, var(--bg-card) 60%)',
              height: '100%',
              transition: 'transform 0.2s, box-shadow 0.2s',
            }}
          >
            <Stethoscope size={28} color="var(--primary)" style={{ marginBottom: 12 }} />
            <h2 style={{ fontSize: '1.125rem', fontWeight: 700, marginBottom: 8 }}>Find a doctor</h2>
            <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', marginBottom: 16 }}>
              See all verified doctors, filter by specialization or hospital, then open available slots.
            </p>
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6, color: 'var(--primary)', fontWeight: 600, fontSize: '0.9rem' }}>
              Browse doctors <ArrowRight size={16} />
            </span>
          </div>
        </Link>

        <Link to="/patient/appointments" style={{ textDecoration: 'none', color: 'inherit' }}>
          <div
            style={{
              borderRadius: 'var(--radius-md)',
              padding: 24,
              border: '1px solid var(--border)',
              background: 'var(--bg-card)',
              height: '100%',
            }}
          >
            <Calendar size={28} color="#1D4ED8" style={{ marginBottom: 12 }} />
            <h2 style={{ fontSize: '1.125rem', fontWeight: 700, marginBottom: 8 }}>Appointments & refunds</h2>
            <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', marginBottom: 16 }}>
              View upcoming visits, join video sessions, or cancel and request a refund.
            </p>
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6, color: '#1D4ED8', fontWeight: 600, fontSize: '0.9rem' }}>
              Open appointments <ArrowRight size={16} />
            </span>
          </div>
        </Link>

        <div
          style={{
            borderRadius: 'var(--radius-md)',
            padding: 24,
            border: '1px solid var(--border)',
            background: 'var(--bg-card)',
          }}
        >
          <Sparkles size={28} color="#7C3AED" style={{ marginBottom: 12 }} />
          <h2 style={{ fontSize: '1.125rem', fontWeight: 700, marginBottom: 8 }}>AI symptom helper</h2>
          <p style={{ fontSize: '0.9rem', color: 'var(--text-secondary)', marginBottom: 16 }}>
            Optional guidance based on symptoms—not a substitute for professional diagnosis.
          </p>
          <Button variant="secondary" size="sm" icon={<Sparkles size={15} />} onClick={() => navigate('/patient/ai')}>
            Open AI assistant
          </Button>
        </div>
      </div>
    </div>
  );
}
