import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { User, Lock, Eye, EyeOff, Heart } from 'lucide-react';
import toast from 'react-hot-toast';
import { useAuth } from '../../context/AuthContext';
import Input from '../../components/common/Input';
import Button from '../../components/common/Button';

export default function LoginPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const [form, setForm] = useState({ username: '', password: '' });
  const [showPass, setShowPass] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});

  const validate = () => {
    const e = {};
    if (!form.username.trim()) e.username = 'Username is required';
    if (!form.password) e.password = 'Password is required';
    return e;
  };

  const handleSubmit = async (ev) => {
    ev.preventDefault();
    const e = validate();
    if (Object.keys(e).length) { setErrors(e); return; }
    setLoading(true);
    const result = await login(form.username, form.password);
    setLoading(false);
    if (result.success) {
      toast.success('Welcome back!');
      const roles = result.user?.roles || [];
      const from = location.state?.from?.pathname;
      if (from && from !== '/login') { navigate(from); return; }
      if (roles.includes('ADMIN')) navigate('/admin/dashboard');
      else if (roles.includes('DOCTOR')) navigate('/doctor/dashboard');
      else navigate('/patient/dashboard');
    } else {
      toast.error(result.message || 'Invalid credentials');
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      background: 'var(--bg)',
    }}>
      {/* Left decorative panel */}
      <div style={{
        flex: '0 0 45%',
        background: 'var(--bg-sidebar)',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        padding: '48px',
        position: 'relative',
        overflow: 'hidden',
      }}>
        {/* Decorative circles */}
        <div style={{
          position: 'absolute',
          width: 400, height: 400,
          borderRadius: '50%',
          background: 'rgba(18,160,143,0.08)',
          top: -100, right: -100,
        }} />
        <div style={{
          position: 'absolute',
          width: 300, height: 300,
          borderRadius: '50%',
          background: 'rgba(18,160,143,0.06)',
          bottom: -60, left: -60,
        }} />
        <div style={{ position: 'relative', zIndex: 1, textAlign: 'center', maxWidth: 360 }}>
          <div style={{
            width: 72, height: 72, borderRadius: 20,
            background: 'var(--primary)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            margin: '0 auto 24px',
            boxShadow: '0 8px 32px rgba(10,110,97,0.4)',
          }}>
            <Heart size={36} color="#fff" fill="#fff" />
          </div>
          <h1 style={{
            fontFamily: 'Sora, sans-serif',
            fontSize: '2rem',
            fontWeight: 700,
            color: '#fff',
            marginBottom: 12,
          }}>
            MediCare
          </h1>
          <p style={{ color: 'var(--text-on-dark-muted)', fontSize: '1rem', lineHeight: 1.7 }}>
            Your trusted healthcare management platform. Connecting patients, doctors, and administrators seamlessly.
          </p>

          {/* Feature pills */}
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 10, marginTop: 36, justifyContent: 'center' }}>
            {['Secure & Private', 'Multi-role Access', 'Real-time Updates'].map((f) => (
              <span key={f} style={{
                padding: '6px 14px',
                borderRadius: '99px',
                background: 'rgba(255,255,255,0.08)',
                color: 'var(--text-on-dark-muted)',
                fontSize: '0.8125rem',
                border: '1px solid rgba(255,255,255,0.1)',
              }}>{f}</span>
            ))}
          </div>
        </div>
      </div>

      {/* Right form panel */}
      <div style={{
        flex: 1,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '48px 32px',
      }}>
        <div style={{ width: '100%', maxWidth: 400, animation: 'fadeIn 0.4s ease' }}>
          <div style={{ marginBottom: 36 }}>
            <h2 style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-primary)', marginBottom: 8 }}>
              Sign in
            </h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9375rem' }}>
              Welcome back — enter your credentials to continue.
            </p>
          </div>

          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
            <Input
              label="Username"
              placeholder="Enter your username"
              value={form.username}
              onChange={(e) => { setForm({ ...form, username: e.target.value }); setErrors({ ...errors, username: '' }); }}
              error={errors.username}
              icon={<User size={16} />}
              autoComplete="username"
            />
            <Input
              label="Password"
              type={showPass ? 'text' : 'password'}
              placeholder="Enter your password"
              value={form.password}
              onChange={(e) => { setForm({ ...form, password: e.target.value }); setErrors({ ...errors, password: '' }); }}
              error={errors.password}
              icon={<Lock size={16} />}
              rightIcon={
                <button type="button" onClick={() => setShowPass((s) => !s)}
                  style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)', display: 'flex' }}>
                  {showPass ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              }
              autoComplete="current-password"
            />

            <Button type="submit" fullWidth loading={loading} size="lg" style={{ marginTop: 4 }}>
              Sign In
            </Button>
          </form>

          <div style={{
            marginTop: 24,
            padding: '16px',
            background: 'var(--success-light)',
            borderRadius: 'var(--radius-sm)',
            border: '1px solid rgba(46,196,182,0.2)',
          }}>
            <p style={{ fontSize: '0.8125rem', color: 'var(--text-secondary)', marginBottom: 4, fontWeight: 500 }}>
              Demo credentials:
            </p>
            <p style={{ fontSize: '0.8125rem', color: 'var(--text-muted)' }}>
              Admin: <strong style={{ color: 'var(--text-primary)' }}>admin / admin123</strong>
            </p>
          </div>

          <p style={{ textAlign: 'center', marginTop: 24, color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
            New doctor?{' '}
            <Link to="/register/doctor" style={{ color: 'var(--primary)', fontWeight: 600 }}>
              Register here
            </Link>
            {' · '}
            <Link to="/register" style={{ color: 'var(--primary)', fontWeight: 600 }}>
              Patient signup
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
}
