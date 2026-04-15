import React, { useEffect, useState, useCallback } from 'react';
import { CheckCircle, XCircle, Stethoscope, Clock, RefreshCw } from 'lucide-react';
import toast from 'react-hot-toast';
import { adminApi } from '../../api/adminApi';
import DashboardLayout from '../../components/layout/DashboardLayout';
import Card from '../../components/common/Card';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';

export default function DoctorApprovalsPage() {
  const [doctors, setDoctors] = useState([]);
  const [loading, setLoading] = useState(true);
  const [approvingId, setApprovingId] = useState(null);
  const [tab, setTab] = useState('pending'); // 'pending' | 'approved'

  const fetchDoctors = useCallback(() => {
    setLoading(true);
    adminApi.getAllUsers()
      .then((res) => {
        const docs = res.data.filter((u) => u.roles?.some((r) => (r.name || r) === 'DOCTOR'));
        setDoctors(docs);
      })
      .catch(() => toast.error('Failed to load doctors'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { fetchDoctors(); }, [fetchDoctors]);

  const handleApprove = async (doctor) => {
    setApprovingId(doctor.id);
    try {
      await adminApi.approveDoctor(doctor.id);
      toast.success(`Dr. ${doctor.username} approved successfully`);
      fetchDoctors();
    } catch {
      toast.error('Approval failed');
    } finally {
      setApprovingId(null);
    }
  };

  const pending = doctors.filter((d) => !d.approved);
  const approved = doctors.filter((d) => d.approved);
  const displayed = tab === 'pending' ? pending : approved;

  return (
    <DashboardLayout>
      <div style={{ maxWidth: 1000, animation: 'fadeIn 0.3s ease' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 28 }}>
          <div>
            <h1 style={{ fontSize: '1.75rem', fontWeight: 700 }}>Doctor Approvals</h1>
            <p style={{ color: 'var(--text-secondary)', marginTop: 4 }}>
              {pending.length} pending · {approved.length} approved
            </p>
          </div>
          <Button icon={<RefreshCw size={15} />} variant="ghost" onClick={fetchDoctors}>Refresh</Button>
        </div>

        {/* Tabs */}
        <div style={{ display: 'flex', gap: 4, marginBottom: 20, background: 'var(--bg-card)', padding: 4, borderRadius: 'var(--radius-sm)', border: '1px solid var(--border)', width: 'fit-content' }}>
          {[
            { key: 'pending', label: `Pending (${pending.length})`, icon: <Clock size={15} /> },
            { key: 'approved', label: `Approved (${approved.length})`, icon: <CheckCircle size={15} /> },
          ].map((t) => (
            <button key={t.key} onClick={() => setTab(t.key)} style={{
              display: 'flex', alignItems: 'center', gap: 6,
              padding: '8px 18px', borderRadius: 6, border: 'none', cursor: 'pointer',
              background: tab === t.key ? 'var(--primary)' : 'transparent',
              color: tab === t.key ? '#fff' : 'var(--text-secondary)',
              fontWeight: 500, fontSize: '0.875rem', transition: 'all 0.15s',
            }}>
              {t.icon}{t.label}
            </button>
          ))}
        </div>

        {loading ? (
          <div style={{ textAlign: 'center', padding: 60, color: 'var(--text-muted)' }}>
            <div style={{ width: 36, height: 36, border: '3px solid var(--border)', borderTopColor: 'var(--primary)', borderRadius: '50%', animation: 'spin 0.8s linear infinite', margin: '0 auto 12px' }} />
            Loading doctors...
          </div>
        ) : displayed.length === 0 ? (
          <div style={{ textAlign: 'center', padding: 60 }}>
            <Stethoscope size={48} style={{ margin: '0 auto 16px', display: 'block', color: 'var(--text-muted)' }} />
            <p style={{ color: 'var(--text-muted)', fontWeight: 500 }}>
              {tab === 'pending' ? 'No pending doctor approvals' : 'No approved doctors yet'}
            </p>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            {displayed.map((doc) => (
              <div key={doc.id} style={{
                background: 'var(--bg-card)',
                borderRadius: 'var(--radius-md)',
                border: `1px solid ${!doc.approved ? 'rgba(240,165,0,0.3)' : 'var(--border)'}`,
                padding: '20px 24px',
                display: 'flex',
                alignItems: 'center',
                gap: 20,
                boxShadow: 'var(--shadow-sm)',
                transition: 'box-shadow 0.2s',
                animation: 'fadeIn 0.3s ease',
              }}
                onMouseEnter={e => e.currentTarget.style.boxShadow = 'var(--shadow-md)'}
                onMouseLeave={e => e.currentTarget.style.boxShadow = 'var(--shadow-sm)'}
              >
                {/* Avatar */}
                <div style={{
                  width: 52, height: 52, borderRadius: '50%',
                  background: doc.approved ? 'var(--primary)' : 'var(--warning)',
                  display: 'flex', alignItems: 'center', justifyContent: 'center',
                  color: '#fff', fontWeight: 700, fontSize: '1.25rem', flexShrink: 0,
                }}>
                  {doc.username?.[0]?.toUpperCase()}
                </div>

                {/* Info */}
                <div style={{ flex: 1, minWidth: 0 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 4 }}>
                    <span style={{ fontWeight: 600, fontSize: '1rem' }}>Dr. {doc.username}</span>
                    <Badge label={doc.approved ? 'Approved' : 'Pending'} variant={doc.approved ? 'APPROVED' : 'PENDING'} />
                  </div>
                  <div style={{ display: 'flex', gap: 20, flexWrap: 'wrap' }}>
                    <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>📧 {doc.email}</span>
                    {doc.phoneNumber && <span style={{ fontSize: '0.85rem', color: 'var(--text-secondary)' }}>📞 {doc.phoneNumber}</span>}
                    <span style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>ID: {doc.id}</span>
                  </div>
                  {doc.createdAt && (
                    <div style={{ fontSize: '0.78rem', color: 'var(--text-muted)', marginTop: 4 }}>
                      Registered: {new Date(doc.createdAt).toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' })}
                    </div>
                  )}
                </div>

                {/* Action */}
                {!doc.approved && (
                  <Button
                    icon={<CheckCircle size={16} />}
                    loading={approvingId === doc.id}
                    onClick={() => handleApprove(doc)}
                    variant="success"
                  >
                    Approve
                  </Button>
                )}
                {doc.approved && (
                  <div style={{ display: 'flex', alignItems: 'center', gap: 6, color: 'var(--success)', fontWeight: 500, fontSize: '0.875rem' }}>
                    <CheckCircle size={18} />
                    Approved
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </DashboardLayout>
  );
}
