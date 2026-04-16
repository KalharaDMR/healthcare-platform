import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { User, Mail, Lock, Phone, Eye, EyeOff, Heart } from 'lucide-react';
import toast from 'react-hot-toast';
import { authApi } from '../../api/authApi';
import Input from '../../components/common/Input';
import Button from '../../components/common/Button';

export default function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', email: '', password: '', confirmPassword: '', phoneNumber: '' });
  const [showPass, setShowPass] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});

  const set = (field) => (e) => {
    setForm((f) => ({ ...f, [field]: e.target.value }));
    setErrors((er) => ({ ...er, [field]: '' }));
  };

  const validate = () => {
    const e = {};
    if (!form.username.trim()) e.username = 'Required';
    if (!form.email.includes('@')) e.email = 'Valid email required';
    if (form.password.length < 6) e.password = 'Min 6 characters';
    if (form.password !== form.confirmPassword) e.confirmPassword = 'Passwords do not match';
    return e;
  };

  const handleSubmit = async (ev) => {
    ev.preventDefault();
    const e = validate();
    if (Object.keys(e).length) { setErrors(e); return; }
    setLoading(true);
    try {
      await authApi.register({
        username: form.username,
        email: form.email,
        password: form.password,
        phoneNumber: form.phoneNumber,
        role: 'PATIENT',
      });
      toast.success('Account created! Please sign in.');
      navigate('/login');
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data || 'Registration failed';
      toast.error(typeof msg === 'string' ? msg : 'Registration failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', background: 'var(--bg)' }}>
      {/* Left panel */}
      <div style={{
        flex: '0 0 40%',
        background: 'var(--bg-sidebar)',
        display: 'flex', flexDirection: 'column',
        justifyContent: 'center', alignItems: 'center',
        padding: 48, position: 'relative', overflow: 'hidden',
      }}>
        <div style={{ position: 'absolute', width: 360, height: 360, borderRadius: '50%', background: 'rgba(18,160,143,0.08)', top: -80, right: -80 }} />
        <div style={{ position: 'absolute', width: 280, height: 280, borderRadius: '50%', background: 'rgba(18,160,143,0.06)', bottom: -60, left: -60 }} />
        <div style={{ position: 'relative', zIndex: 1, textAlign: 'center', maxWidth: 320 }}>
          <div style={{ width: 64, height: 64, borderRadius: 18, background: 'var(--primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 20px', boxShadow: '0 8px 32px rgba(10,110,97,0.4)' }}>
            <Heart size={30} color="#fff" fill="#fff" />
          </div>
          <h2 style={{ fontFamily: 'Sora,sans-serif', fontSize: '1.75rem', fontWeight: 700, color: '#fff', marginBottom: 12 }}>
            Join as Patient
          </h2>
          <p style={{ color: 'var(--text-on-dark-muted)', lineHeight: 1.7 }}>
            Create your free account to book appointments, track your health, and connect with doctors.
          </p>
        </div>
      </div>

      {/* Right form */}
      <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '48px 32px' }}>
        <div style={{ width: '100%', maxWidth: 420, animation: 'fadeIn 0.4s ease' }}>
          <div style={{ marginBottom: 32 }}>
            <h2 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: 6 }}>Create account</h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Patient registration — fill in your details below.</p>
          </div>

          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
            <Input label="Username" placeholder="Choose a username" value={form.username} onChange={set('username')} error={errors.username} icon={<User size={16} />} />
            <Input label="Email" type="email" placeholder="your@email.com" value={form.email} onChange={set('email')} error={errors.email} icon={<Mail size={16} />} />
            <Input label="Phone Number" type="tel" placeholder="+1 234 567 8900" value={form.phoneNumber} onChange={set('phoneNumber')} icon={<Phone size={16} />} />
            <Input
              label="Password"
              type={showPass ? 'text' : 'password'}
              placeholder="Min 6 characters"
              value={form.password} onChange={set('password')} error={errors.password}
              icon={<Lock size={16} />}
              rightIcon={
                <button type="button" onClick={() => setShowPass(s => !s)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)', display: 'flex' }}>
                  {showPass ? <EyeOff size={16} /> : <Eye size={16} />}
                </button>
              }
            />
            <Input label="Confirm Password" type="password" placeholder="Repeat password" value={form.confirmPassword} onChange={set('confirmPassword')} error={errors.confirmPassword} icon={<Lock size={16} />} />

            <Button type="submit" fullWidth loading={loading} size="lg" style={{ marginTop: 4 }}>
              Create Account
            </Button>
          </form>

          <p style={{ textAlign: 'center', marginTop: 20, color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
            Already have an account?{' '}
            <Link to="/login" style={{ color: 'var(--primary)', fontWeight: 600 }}>Sign in</Link>
            {' · '}
            <Link to="/register/doctor" style={{ color: 'var(--primary)', fontWeight: 600 }}>Doctor signup</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
