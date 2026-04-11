import React from 'react';
import { useNavigate } from 'react-router-dom';
import { ShieldOff } from 'lucide-react';
import Button from '../../components/common/Button';

export default function UnauthorizedPage() {
  const navigate = useNavigate();
  return (
    <div style={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'var(--bg)', flexDirection: 'column', gap: 20, textAlign: 'center', padding: 24 }}>
      <div style={{ width: 80, height: 80, borderRadius: 24, background: 'var(--danger-light)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
        <ShieldOff size={40} color="var(--danger)" />
      </div>
      <h1 style={{ fontSize: '2rem', fontWeight: 700 }}>Access Denied</h1>
      <p style={{ color: 'var(--text-secondary)', maxWidth: 340 }}>You don't have permission to view this page.</p>
      <Button onClick={() => navigate(-1)}>Go Back</Button>
    </div>
  );
}
