import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  Activity,
  ArrowRight,
  CalendarCheck2,
  HeartPulse,
  Menu,
  ShieldCheck,
  Sparkles,
  Stethoscope,
  Video,
  X,
} from 'lucide-react';
import Button from '../../components/common/Button';
import { useAuth } from '../../context/AuthContext';

export default function HomePage() {
  const navigate = useNavigate();
  const { user, isAdmin, isDoctor } = useAuth();
  const [mobileOpen, setMobileOpen] = useState(false);

  const goToDashboard = () => {
    if (!user) {
      navigate('/login');
      return;
    }
    if (isAdmin) navigate('/admin/dashboard');
    else if (isDoctor) navigate('/doctor/dashboard');
    else navigate('/patient/dashboard');
  };

  return (
    <div style={{ minHeight: '100vh', background: 'var(--bg)' }}>
      <header
        style={{
          position: 'sticky',
          top: 0,
          zIndex: 50,
          backdropFilter: 'blur(12px)',
          background: 'rgba(244,247,246,0.8)',
          borderBottom: '1px solid rgba(221,232,230,0.8)',
        }}
      >
        <div
          style={{
            maxWidth: 1200,
            margin: '0 auto',
            padding: '14px 24px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <div
              style={{
                width: 42,
                height: 42,
                borderRadius: 12,
                background: 'var(--primary)',
                display: 'grid',
                placeItems: 'center',
                boxShadow: 'var(--shadow-md)',
              }}
            >
              <HeartPulse size={22} color="#fff" />
            </div>
            <div>
              <div style={{ fontFamily: 'Sora, sans-serif', fontWeight: 700, fontSize: '1.1rem' }}>MediCare</div>
              <div style={{ color: 'var(--text-muted)', fontSize: '0.76rem', letterSpacing: '0.06em' }}>
                SMART HEALTH PLATFORM
              </div>
            </div>
          </div>

          <nav style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
            <Link to="/register" style={{ fontWeight: 600, color: 'var(--text-secondary)' }}>
              Patient Signup
            </Link>
            <Link to="/register/doctor" style={{ fontWeight: 600, color: 'var(--text-secondary)' }}>
              Doctor Signup
            </Link>
            {!user ? (
              <Button onClick={() => navigate('/login')}>Sign In</Button>
            ) : (
              <Button onClick={goToDashboard}>Go to Dashboard</Button>
            )}
            <button
              type="button"
              onClick={() => setMobileOpen((s) => !s)}
              style={{
                width: 38,
                height: 38,
                borderRadius: 10,
                background: 'var(--bg-card)',
                border: '1px solid var(--border)',
                display: 'grid',
                placeItems: 'center',
                color: 'var(--text-secondary)',
              }}
            >
              {mobileOpen ? <X size={18} /> : <Menu size={18} />}
            </button>
          </nav>
        </div>
        {mobileOpen && (
          <div
            style={{
              maxWidth: 1200,
              margin: '0 auto',
              padding: '0 24px 14px',
            }}
          >
            <div
              style={{
                background: 'var(--bg-card)',
                border: '1px solid var(--border)',
                borderRadius: 12,
                padding: 12,
                display: 'grid',
                gap: 8,
              }}
            >
              <Button variant="ghost" onClick={() => { setMobileOpen(false); navigate('/register'); }}>
                Patient Signup
              </Button>
              <Button variant="ghost" onClick={() => { setMobileOpen(false); navigate('/register/doctor'); }}>
                Doctor Signup
              </Button>
              {!user ? (
                <Button onClick={() => { setMobileOpen(false); navigate('/login'); }}>Sign In</Button>
              ) : (
                <Button onClick={() => { setMobileOpen(false); goToDashboard(); }}>Go to Dashboard</Button>
              )}
            </div>
          </div>
        )}
      </header>

      <main style={{ maxWidth: 1200, margin: '0 auto', padding: '28px 24px 48px' }}>
        <section
          style={{
            background: 'linear-gradient(125deg, #072E2A 0%, #0A6E61 58%, #12A08F 100%)',
            borderRadius: 24,
            padding: '56px 44px',
            color: '#fff',
            boxShadow: 'var(--shadow-lg)',
            position: 'relative',
            overflow: 'hidden',
          }}
        >
          <div
            style={{
              position: 'absolute',
              width: 260,
              height: 260,
              borderRadius: '50%',
              background: 'rgba(255,255,255,0.1)',
              right: -70,
              top: -80,
            }}
          />
          <div
            style={{
              position: 'absolute',
              width: 220,
              height: 220,
              borderRadius: '50%',
              background: 'rgba(255,255,255,0.08)',
              left: -80,
              bottom: -90,
            }}
          />
          <div style={{ position: 'relative', zIndex: 1, maxWidth: 760 }}>
            <span
              style={{
                display: 'inline-block',
                padding: '6px 12px',
                borderRadius: 99,
                background: 'rgba(255,255,255,0.15)',
                fontSize: '0.78rem',
                fontWeight: 600,
                marginBottom: 14,
              }}
            >
              AI-enabled Smart Healthcare
            </span>
            <h1 style={{ fontSize: '2.4rem', fontWeight: 700, lineHeight: 1.2, marginBottom: 12 }}>
              Book Appointments, Attend Video Consultations, and Manage Care Seamlessly
            </h1>
            <p style={{ color: 'rgba(255,255,255,0.92)', maxWidth: 650, fontSize: '1rem', marginBottom: 24 }}>
              A modern healthcare platform for patients, doctors, and administrators with telemedicine, role-based
              workflows, secure payments, and AI symptom support.
            </p>
            <div style={{ display: 'flex', gap: 12, flexWrap: 'wrap' }}>
              <Button onClick={goToDashboard} size="lg">
                {user ? 'Open Dashboard' : 'Get Started'}
              </Button>
              <Button variant="secondary" size="lg" onClick={() => navigate('/register/doctor')}>
                Join as Doctor
              </Button>
            </div>
          </div>
        </section>

        <section style={{ marginTop: 18 }}>
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(180px, 1fr))',
              gap: 12,
            }}
          >
            {[
              { value: '12K+', label: 'Appointments Processed' },
              { value: '350+', label: 'Verified Doctors' },
              { value: '24/7', label: 'Platform Availability' },
              { value: '95%', label: 'Patient Satisfaction' },
            ].map((metric, idx) => (
              <div
                key={metric.label}
                style={{
                  background: 'var(--bg-card)',
                  border: '1px solid var(--border)',
                  borderRadius: 12,
                  padding: '14px 16px',
                  boxShadow: 'var(--shadow-sm)',
                  animation: `fadeIn 0.35s ease ${idx * 0.08}s both`,
                }}
              >
                <div style={{ fontFamily: 'Sora, sans-serif', fontSize: '1.45rem', fontWeight: 700 }}>{metric.value}</div>
                <div style={{ color: 'var(--text-secondary)', fontSize: '0.84rem' }}>{metric.label}</div>
              </div>
            ))}
          </div>
        </section>

        <section style={{ marginTop: 30 }}>
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
              gap: 16,
            }}
          >
            {[
              {
                icon: <CalendarCheck2 size={20} />,
                title: 'Appointment Management',
                text: 'Search doctors, book sessions, and track real-time status updates.',
              },
              {
                icon: <Video size={20} />,
                title: 'Telemedicine Sessions',
                text: 'Secure video consultations with tokenized access and role-based joining.',
              },
              {
                icon: <ShieldCheck size={20} />,
                title: 'Role-Based Security',
                text: 'Dedicated access for patients, doctors, and admins with JWT auth.',
              },
              {
                icon: <Activity size={20} />,
                title: 'AI Symptom Guidance',
                text: 'Optional AI-assisted symptom analysis with recommended specializations.',
              },
            ].map((item, idx) => (
              <div
                key={item.title}
                style={{
                  border: '1px solid var(--border)',
                  background: 'var(--bg-card)',
                  borderRadius: 14,
                  padding: 18,
                  boxShadow: 'var(--shadow-sm)',
                  transition: 'transform 0.18s ease, box-shadow 0.18s ease',
                  animation: `fadeIn 0.4s ease ${idx * 0.06}s both`,
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.transform = 'translateY(-3px)';
                  e.currentTarget.style.boxShadow = 'var(--shadow-md)';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.transform = 'translateY(0)';
                  e.currentTarget.style.boxShadow = 'var(--shadow-sm)';
                }}
              >
                <div
                  style={{
                    width: 40,
                    height: 40,
                    borderRadius: 10,
                    background: 'rgba(10,110,97,0.12)',
                    display: 'grid',
                    placeItems: 'center',
                    color: 'var(--primary)',
                    marginBottom: 10,
                  }}
                >
                  {item.icon}
                </div>
                <h3 style={{ fontSize: '1rem', marginBottom: 6 }}>{item.title}</h3>
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', lineHeight: 1.6 }}>{item.text}</p>
              </div>
            ))}
          </div>
        </section>

        <section
          style={{
            marginTop: 24,
            display: 'grid',
            gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))',
            gap: 16,
          }}
        >
          <div
            style={{
              background: 'var(--bg-card)',
              border: '1px solid var(--border)',
              borderRadius: 16,
              padding: 20,
              boxShadow: 'var(--shadow-sm)',
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 8 }}>
              <Sparkles size={16} color="var(--accent)" />
              <span style={{ fontWeight: 600, fontSize: '0.9rem' }}>Why teams like this platform</span>
            </div>
            <h3 style={{ fontSize: '1.2rem', marginBottom: 8 }}>Designed for real healthcare workflows</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.92rem', marginBottom: 14 }}>
              Streamline doctor discovery, availability scheduling, secure payments, telemedicine calls, and
              prescriptions in one unified experience.
            </p>
            <div style={{ display: 'grid', gap: 10 }}>
              {[
                'Fast patient onboarding with role-based access.',
                'Doctor availability and appointment lifecycle automation.',
                'Built-in integration points for payment and video APIs.',
              ].map((line) => (
                <div key={line} style={{ display: 'flex', alignItems: 'center', gap: 8, color: 'var(--text-secondary)', fontSize: '0.88rem' }}>
                  <ArrowRight size={14} color="var(--primary)" />
                  {line}
                </div>
              ))}
            </div>
          </div>

          <div
            style={{
              display: 'grid',
              gap: 12,
            }}
          >
            {[
              {
                quote:
                  'Booking and telemedicine flow feels smooth. The dashboard gives us everything in one place.',
                by: 'Patient User',
              },
              {
                quote:
                  'Managing slots and handling consultations is very straightforward. Great productivity boost.',
                by: 'Doctor User',
              },
            ].map((t, idx) => (
              <div
                key={t.by}
                style={{
                  background: 'var(--bg-card)',
                  border: '1px solid var(--border)',
                  borderRadius: 14,
                  padding: 16,
                  boxShadow: 'var(--shadow-sm)',
                  animation: `fadeIn 0.35s ease ${idx * 0.08}s both`,
                }}
              >
                <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem', fontStyle: 'italic', marginBottom: 8 }}>
                  "{t.quote}"
                </p>
                <div style={{ fontWeight: 600, fontSize: '0.85rem' }}>{t.by}</div>
              </div>
            ))}
          </div>
        </section>

        <section
          style={{
            marginTop: 26,
            border: '1px solid var(--border)',
            background: 'var(--bg-card)',
            borderRadius: 16,
            padding: '22px 18px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: 16,
            flexWrap: 'wrap',
          }}
        >
          <div>
            <h3 style={{ fontSize: '1.05rem', marginBottom: 4 }}>Ready to use the platform?</h3>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
              Patients can register and channel doctors. Doctors can manage availability and consultations.
            </p>
          </div>
          <div style={{ display: 'flex', gap: 10 }}>
            <Button variant="ghost" onClick={() => navigate('/register')}>
              Register as Patient
            </Button>
            <Button onClick={() => navigate('/register/doctor')}>
              <Stethoscope size={16} />
              Register as Doctor
            </Button>
          </div>
        </section>
      </main>
    </div>
  );
}
