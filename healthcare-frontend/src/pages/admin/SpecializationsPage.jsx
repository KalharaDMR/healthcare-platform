import React, { useEffect, useState, useCallback } from 'react';
import { Plus, Stethoscope, CheckCircle } from 'lucide-react';
import toast from 'react-hot-toast';
import { adminApi } from '../../api/adminApi';
import DashboardLayout from '../../components/layout/DashboardLayout';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import Modal from '../../components/common/Modal';

export default function SpecializationsPage() {
  const [specs, setSpecs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [addOpen, setAddOpen] = useState(false);
  const [newName, setNewName] = useState('');
  const [addLoading, setAddLoading] = useState(false);
  const [nameError, setNameError] = useState('');

  const fetchSpecs = useCallback(() => {
    setLoading(true);
    adminApi.getSpecializations()
      .then((res) => setSpecs(res.data))
      .catch(() => toast.error('Failed to load specializations'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { fetchSpecs(); }, [fetchSpecs]);

  const handleAdd = async () => {
    if (!newName.trim()) { setNameError('Name is required'); return; }
    setAddLoading(true);
    try {
      await adminApi.addSpecialization(newName.trim());
      toast.success(`"${newName}" added`);
      setNewName('');
      setAddOpen(false);
      fetchSpecs();
    } catch (err) {
      const msg = err.response?.data?.error || 'Failed to add';
      toast.error(msg);
    } finally {
      setAddLoading(false);
    }
  };

  const colors = [
    'var(--primary)', '#1D4ED8', '#7C3AED', '#DB2777', '#D97706',
    '#059669', '#0891B2', '#DC2626', '#65A30D', '#9333EA',
  ];

  return (
    <DashboardLayout>
      <div style={{ maxWidth: 800, animation: 'fadeIn 0.3s ease' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 28 }}>
          <div>
            <h1 style={{ fontSize: '1.75rem', fontWeight: 700 }}>Specializations</h1>
            <p style={{ color: 'var(--text-secondary)', marginTop: 4 }}>{specs.length} specializations available</p>
          </div>
          <Button icon={<Plus size={16} />} onClick={() => setAddOpen(true)}>
            Add Specialization
          </Button>
        </div>

        <Card>
          {loading ? (
            <div style={{ textAlign: 'center', padding: 40, color: 'var(--text-muted)' }}>
              <div style={{ width: 32, height: 32, border: '3px solid var(--border)', borderTopColor: 'var(--primary)', borderRadius: '50%', animation: 'spin 0.8s linear infinite', margin: '0 auto 12px' }} />
              Loading...
            </div>
          ) : specs.length === 0 ? (
            <div style={{ textAlign: 'center', padding: 48, color: 'var(--text-muted)' }}>
              <Stethoscope size={40} style={{ margin: '0 auto 12px', display: 'block' }} />
              No specializations found.
            </div>
          ) : (
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))', gap: 14 }}>
              {specs.map((spec, i) => {
                const color = colors[i % colors.length];
                return (
                  <div key={spec} style={{
                    padding: '18px 20px',
                    borderRadius: 'var(--radius-md)',
                    border: '1.5px solid',
                    borderColor: `${color}30`,
                    background: `${color}08`,
                    display: 'flex',
                    alignItems: 'center',
                    gap: 12,
                    transition: 'all 0.2s',
                    cursor: 'default',
                    animation: `fadeIn ${0.1 + i * 0.04}s ease`,
                  }}
                    onMouseEnter={e => { e.currentTarget.style.borderColor = `${color}60`; e.currentTarget.style.transform = 'translateY(-2px)'; e.currentTarget.style.boxShadow = `0 4px 16px ${color}20`; }}
                    onMouseLeave={e => { e.currentTarget.style.borderColor = `${color}30`; e.currentTarget.style.transform = ''; e.currentTarget.style.boxShadow = ''; }}
                  >
                    <div style={{ width: 38, height: 38, borderRadius: 10, background: `${color}15`, display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0 }}>
                      <Stethoscope size={18} color={color} />
                    </div>
                    <div>
                      <div style={{ fontWeight: 600, fontSize: '0.9rem', color: 'var(--text-primary)' }}>{spec}</div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginTop: 2 }}>Active</div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </Card>

        {/* Add Modal */}
        <Modal open={addOpen} onClose={() => { setAddOpen(false); setNewName(''); setNameError(''); }} title="Add Specialization" size="sm">
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>
              Add a new medical specialization that doctors can register under.
            </p>
            <Input
              label="Specialization Name"
              placeholder="e.g. Oncology, Psychiatry..."
              value={newName}
              onChange={(e) => { setNewName(e.target.value); setNameError(''); }}
              error={nameError}
              icon={<Stethoscope size={15} />}
              onKeyDown={(e) => { if (e.key === 'Enter') handleAdd(); }}
              autoFocus
            />
            <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end' }}>
              <Button variant="ghost" onClick={() => { setAddOpen(false); setNewName(''); }}>Cancel</Button>
              <Button loading={addLoading} onClick={handleAdd} icon={<Plus size={15} />}>Add</Button>
            </div>
          </div>
        </Modal>
      </div>
    </DashboardLayout>
  );
}
