import React from 'react';
import { Stethoscope, Users, Calendar } from 'lucide-react';
import DashboardLayout from '../../components/layout/DashboardLayout';
import { useAuth } from '../../context/AuthContext';

export default function DoctorDashboard() {
  const { user } = useAuth();

  return (
    <DashboardLayout>
      <div style={{ maxWidth: 900, animation: 'fadeIn 0.3s ease' }}>
        <div style={{ marginBottom: 32 }}>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 700 }}>
            Welcome, Dr. {user?.username} 👋
          </h1>
          <p style={{ color: 'var(--text-secondary)', marginTop: 4 }}>Your doctor dashboard</p>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 20, marginBottom: 32 }}>
          {[
            { icon: <Users size={24} />, label: 'My Patients', value: '0', color: 'var(--primary)', bg: 'rgba(10,110,97,0.1)' },
            { icon: <Calendar size={24} />, label: "Today's Appointments", value: '0', color: '#1D4ED8', bg: '#DBEAFE' },
            { icon: <Stethoscope size={24} />, label: 'Consultations', value: '0', color: '#7C3AED', bg: '#EDE9FE' },
          ].map((card) => (
            <div key={card.label} style={{ background: 'var(--bg-card)', borderRadius: 'var(--radius-md)', padding: 24, border: '1px solid var(--border)', boxShadow: 'var(--shadow-sm)', display: 'flex', alignItems: 'center', gap: 16 }}>
              <div style={{ width: 50, height: 50, borderRadius: 14, background: card.bg, display: 'flex', alignItems: 'center', justifyContent: 'center', color: card.color }}>
                {card.icon}
              </div>
              <div>
                <div style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', fontWeight: 500 }}>{card.label}</div>
                <div style={{ fontSize: '1.75rem', fontWeight: 700, fontFamily: 'Sora,sans-serif' }}>{card.value}</div>
              </div>
            </div>
          ))}
        </div>

        <div style={{ background: 'var(--bg-card)', borderRadius: 'var(--radius-md)', padding: 40, border: '1px solid var(--border)', textAlign: 'center' }}>
          <Stethoscope size={48} style={{ color: 'var(--text-muted)', margin: '0 auto 16px', display: 'block' }} />
          <h3 style={{ fontSize: '1.125rem', fontWeight: 600, marginBottom: 8 }}>Doctor features coming soon</h3>
          <p style={{ color: 'var(--text-muted)', maxWidth: 360, margin: '0 auto' }}>
            Manage appointments, view patient records, and write prescriptions — features will be added as your backend grows.
          </p>
        </div>
      </div>
    </DashboardLayout>
  );
}
