import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { User, Mail, Lock, Phone, FileText, Stethoscope, Eye, EyeOff } from 'lucide-react';
import toast from 'react-hot-toast';
import { authApi } from '../../api/authApi';
import { adminApi } from '../../api/adminApi';
import Input from '../../components/common/Input';
import Button from '../../components/common/Button';

export default function DoctorRegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: '',
    doctorName: '',
    email: '',
    password: '',
    confirmPassword: '',
    phoneNumber: '',
    specialization: '',
    licenseNumber: '',
  });
  const [specializations, setSpecializations] = useState([]);
  const [showPass, setShowPass] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errors, setErrors] = useState({});

  useEffect(() => {
    adminApi.getSpecializations()
      .then((res) => setSpecializations(res.data))
      .catch(() => setSpecializations(['Cardiology', 'Dermatology', 'Pediatrics', 'Neurology', 'Orthopedics']));
  }, []);

  const set = (field) => (e) => {
    setForm((f) => ({ ...f, [field]: e.target.value }));
    setErrors((er) => ({ ...er, [field]: '' }));
  };

  const validate = () => {
    const e = {};
    if (!form.username.trim()) e.username = 'Required';
    if (!form.doctorName.trim()) e.doctorName = 'Enter your name as it should appear to patients';
    if (!form.email.includes('@')) e.email = 'Valid email required';
    if (form.password.length < 6) e.password = 'Min 6 characters';
    if (form.password !== form.confirmPassword) e.confirmPassword = 'Passwords do not match';
    if (!form.specialization) e.specialization = 'Please select a specialization';
    return e;
  };

  const handleSubmit = async (ev) => {
    ev.preventDefault();
    const e = validate();
    if (Object.keys(e).length) { setErrors(e); return; }
    setLoading(true);
    try {
      await authApi.registerDoctor({
        username: form.username.trim(),
        doctorName: form.doctorName.trim(),
        email: form.email.trim(),
        password: form.password,
        phoneNumber: form.phoneNumber.trim(),
        specialization: form.specialization,
        licenseNumber: form.licenseNumber.trim(),
      });
      toast.success('Registration submitted! Awaiting admin approval.');
      navigate('/login');
    } catch (err) {
      const msg = err.response?.data?.error || err.response?.data?.message || 'Registration failed';
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', background: 'var(--bg)' }}>
      {/* Left panel */}
      <div style={{
        flex: '0 0 38%',
        background: 'linear-gradient(160deg, #072E2A 0%, #0A4039 60%, #064E45 100%)',
        display: 'flex', flexDirection: 'column',
        justifyContent: 'center', alignItems: 'center',
        padding: 48, position: 'relative', overflow: 'hidden',
      }}>
        <div style={{ position: 'absolute', width: 400, height: 400, borderRadius: '50%', background: 'rgba(240,165,0,0.06)', top: -100, right: -100 }} />
        <div style={{ position: 'absolute', width: 250, height: 250, borderRadius: '50%', background: 'rgba(18,160,143,0.08)', bottom: -40, left: -40 }} />
        <div style={{ position: 'relative', zIndex: 1, textAlign: 'center', maxWidth: 300 }}>
          <div style={{ width: 68, height: 68, borderRadius: 20, background: 'var(--accent)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 20px', boxShadow: '0 8px 32px rgba(240,165,0,0.3)' }}>
            <Stethoscope size={34} color="#fff" />
          </div>
          <h2 style={{ fontFamily: 'Sora,sans-serif', fontSize: '1.75rem', fontWeight: 700, color: '#fff', marginBottom: 12 }}>
            Doctor Registration
          </h2>
          <p style={{ color: 'var(--text-on-dark-muted)', lineHeight: 1.7, fontSize: '0.9375rem' }}>
            Join our network of healthcare professionals. Your account will be reviewed and approved by an administrator.
          </p>
          <div style={{ marginTop: 28, padding: '14px 18px', background: 'rgba(240,165,0,0.1)', borderRadius: 10, border: '1px solid rgba(240,165,0,0.2)' }}>
            <p style={{ color: 'var(--accent-light)', fontSize: '0.8125rem', lineHeight: 1.6 }}>
              ⏱ Approval usually takes 1–2 business days after submission.
            </p>
          </div>
        </div>
      </div>

      {/* Right form */}
      <div style={{ flex: 1, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '40px 32px', overflowY: 'auto' }}>
        <div style={{ width: '100%', maxWidth: 460, animation: 'fadeIn 0.4s ease' }}>
          <div style={{ marginBottom: 28 }}>
            <h2 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: 6 }}>Create Doctor Account</h2>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Fill in your professional details below.</p>
          </div>

          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            {/* Personal info */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14 }}>
              <Input label="Username" placeholder="dr.username" value={form.username} onChange={set('username')} error={errors.username} icon={<User size={15} />} />
              <Input label="Phone" type="tel" placeholder="+1 234 567" value={form.phoneNumber} onChange={set('phoneNumber')} icon={<Phone size={15} />} />
            </div>
            <Input
              label="Doctor name"
              placeholder="e.g. Dr. Jane Perera"
              value={form.doctorName}
              onChange={set('doctorName')}
              error={errors.doctorName}
              icon={<Stethoscope size={15} />}
            />
            <Input label="Email" type="email" placeholder="doctor@clinic.com" value={form.email} onChange={set('email')} error={errors.email} icon={<Mail size={15} />} />

            {/* Specialization */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
              <label style={{ fontSize: '0.875rem', fontWeight: 500, color: errors.specialization ? 'var(--danger)' : 'var(--text-secondary)' }}>
                Specialization
              </label>
              <div style={{ position: 'relative' }}>
                <span style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)', display: 'flex', pointerEvents: 'none' }}>
                  <Stethoscope size={15} />
                </span>
                <select
                  value={form.specialization}
                  onChange={set('specialization')}
                  style={{
                    width: '100%', padding: '11px 14px 11px 40px',
                    border: `1.5px solid ${errors.specialization ? 'var(--danger)' : 'var(--border)'}`,
                    borderRadius: 'var(--radius-sm)',
                    fontSize: '0.9375rem', color: form.specialization ? 'var(--text-primary)' : 'var(--text-muted)',
                    background: 'var(--bg-card)', outline: 'none', cursor: 'pointer',
                    appearance: 'none',
                  }}
                >
                  <option value="">Select specialization...</option>
                  {specializations.map((s) => <option key={s} value={s}>{s}</option>)}
                </select>
              </div>
              {errors.specialization && <span style={{ fontSize: '0.8rem', color: 'var(--danger)' }}>{errors.specialization}</span>}
            </div>

            <Input label="License Number (optional)" placeholder="e.g. LIC-123456" value={form.licenseNumber} onChange={set('licenseNumber')} icon={<FileText size={15} />} />

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 14 }}>
              <Input
                label="Password"
                type={showPass ? 'text' : 'password'}
                placeholder="Min 6 chars"
                value={form.password} onChange={set('password')} error={errors.password}
                icon={<Lock size={15} />}
                rightIcon={
                  <button type="button" onClick={() => setShowPass(s => !s)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: 'var(--text-muted)', display: 'flex' }}>
                    {showPass ? <EyeOff size={15} /> : <Eye size={15} />}
                  </button>
                }
              />
              <Input label="Confirm Password" type="password" placeholder="Repeat" value={form.confirmPassword} onChange={set('confirmPassword')} error={errors.confirmPassword} icon={<Lock size={15} />} />
            </div>

            <Button type="submit" fullWidth loading={loading} size="lg" style={{ marginTop: 4 }}>
              Submit for Approval
            </Button>
          </form>

          <p style={{ textAlign: 'center', marginTop: 18, color: 'var(--text-secondary)', fontSize: '0.875rem' }}>
            Already approved?{' '}
            <Link to="/login" style={{ color: 'var(--primary)', fontWeight: 600 }}>Sign in</Link>
            {' · '}
            <Link to="/register" style={{ color: 'var(--primary)', fontWeight: 600 }}>Patient signup</Link>
          </p>
        </div>
      </div>
    </div>
  );
}
