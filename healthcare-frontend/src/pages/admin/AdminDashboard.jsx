import React, { useEffect, useState } from 'react';
import { Users, Stethoscope, UserCheck, Clock } from 'lucide-react';
import { adminApi } from '../../api/adminApi';
import DashboardLayout from '../../components/layout/DashboardLayout';
import Card from '../../components/common/Card';
import Badge from '../../components/common/Badge';

function StatCard({ icon, label, value, color, bg }) {
  return (
    <div style={{
      background: 'var(--bg-card)',
      borderRadius: 'var(--radius-md)',
      padding: '24px',
      border: '1px solid var(--border)',
      boxShadow: 'var(--shadow-sm)',
      display: 'flex',
      alignItems: 'center',
      gap: 20,
      animation: 'fadeIn 0.35s ease',
    }}>
      <div style={{ width: 52, height: 52, borderRadius: 14, background: bg, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
        {React.cloneElement(icon, { size: 24, color })}
      </div>
      <div>
        <div style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', fontWeight: 500, marginBottom: 4 }}>{label}</div>
        <div style={{ fontSize: '1.75rem', fontWeight: 700, fontFamily: 'Sora,sans-serif', color: 'var(--text-primary)' }}>{value}</div>
      </div>
    </div>
  );
}

export default function AdminDashboard() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    adminApi.getAllUsers()
      .then((res) => setUsers(res.data))
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  const totalUsers = users.length;
  const doctors = users.filter((u) => u.roles?.some((r) => (r.name || r) === 'DOCTOR'));
  const pendingDoctors = doctors.filter((d) => !d.approved);
  const patients = users.filter((u) => u.roles?.some((r) => (r.name || r) === 'PATIENT'));

  const recentUsers = [...users].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)).slice(0, 5);

  return (
    <DashboardLayout>
      <div style={{ maxWidth: 1200, animation: 'fadeIn 0.3s ease' }}>
        <div style={{ marginBottom: 32 }}>
          <h1 style={{ fontSize: '1.75rem', fontWeight: 700, color: 'var(--text-primary)' }}>Dashboard</h1>
          <p style={{ color: 'var(--text-secondary)', marginTop: 4 }}>Overview of the healthcare platform</p>
        </div>

        {/* Stats grid */}
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 20, marginBottom: 32 }}>
          <StatCard icon={<Users />} label="Total Users" value={loading ? '—' : totalUsers} color="var(--primary)" bg="rgba(10,110,97,0.1)" />
          <StatCard icon={<Stethoscope />} label="Doctors" value={loading ? '—' : doctors.length} color="#1D4ED8" bg="#DBEAFE" />
          <StatCard icon={<UserCheck />} label="Patients" value={loading ? '—' : patients.length} color="#065F46" bg="#D1FAE5" />
          <StatCard icon={<Clock />} label="Pending Approvals" value={loading ? '—' : pendingDoctors.length} color="var(--warning)" bg="var(--warning-light)" />
        </div>

        {/* Recent users table */}
        <Card title="Recent Users" subtitle="Newest registered accounts">
          {loading ? (
            <div style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>Loading...</div>
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ borderBottom: '1px solid var(--border)' }}>
                    {['User', 'Email', 'Role', 'Status', 'Approved'].map((h) => (
                      <th key={h} style={{ padding: '10px 16px', textAlign: 'left', fontSize: '0.8rem', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {recentUsers.map((u) => (
                    <tr key={u.id} style={{ borderBottom: '1px solid var(--border)', transition: 'background 0.15s' }}
                      onMouseEnter={e => e.currentTarget.style.background = 'var(--bg)'}
                      onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
                    >
                      <td style={{ padding: '12px 16px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                          <div style={{ width: 32, height: 32, borderRadius: '50%', background: 'var(--primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontSize: '0.8rem', fontWeight: 700 }}>
                            {u.username?.[0]?.toUpperCase()}
                          </div>
                          <span style={{ fontWeight: 500 }}>{u.username}</span>
                        </div>
                      </td>
                      <td style={{ padding: '12px 16px', color: 'var(--text-secondary)', fontSize: '0.9rem' }}>{u.email}</td>
                      <td style={{ padding: '12px 16px' }}>
                        <div style={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
                          {(u.roles || []).map((r) => <Badge key={r.id || r.name} label={r.name || r} />)}
                        </div>
                      </td>
                      <td style={{ padding: '12px 16px' }}><Badge label={u.status || 'ACTIVE'} /></td>
                      <td style={{ padding: '12px 16px' }}><Badge label={u.approved ? 'Approved' : 'Pending'} variant={u.approved ? 'APPROVED' : 'PENDING'} /></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Card>
      </div>
    </DashboardLayout>
  );
}
