import React, { useEffect, useState, useCallback } from 'react';
import { Search, Trash2, Edit2, Shield, UserCheck, Plus, RefreshCw } from 'lucide-react';
import toast from 'react-hot-toast';
import { adminApi } from '../../api/adminApi';
import DashboardLayout from '../../components/layout/DashboardLayout';
import Card from '../../components/common/Card';
import Badge from '../../components/common/Badge';
import Button from '../../components/common/Button';
import Modal from '../../components/common/Modal';
import Input from '../../components/common/Input';

const ROLES = ['PATIENT', 'DOCTOR', 'ADMIN'];

function EditUserModal({ user, open, onClose, onSaved }) {
  const [form, setForm] = useState({ email: '', phoneNumber: '', status: '', password: '', specialization: '', licenseNumber: '' });
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (user) {
      setForm({
        email: user.email || '',
        phoneNumber: user.phoneNumber || '',
        status: user.status || 'ACTIVE',
        password: '',
        specialization: '',
        licenseNumber: '',
      });
    }
  }, [user]);

  const isDoctor = user?.roles?.some((r) => (r.name || r) === 'DOCTOR');

  const handleSave = async () => {
    setLoading(true);
    try {
      const payload = { ...form };
      if (!payload.password) delete payload.password;
      await adminApi.updateUser(user.id, payload);
      toast.success('User updated successfully');
      onSaved();
      onClose();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Update failed');
    } finally {
      setLoading(false);
    }
  };

  const set = (field) => (e) => setForm((f) => ({ ...f, [field]: e.target.value }));

  return (
    <Modal open={open} onClose={onClose} title={`Edit User — ${user?.username}`}>
      <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
        <Input label="Email" type="email" value={form.email} onChange={set('email')} />
        <Input label="Phone Number" value={form.phoneNumber} onChange={set('phoneNumber')} />
        <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
          <label style={{ fontSize: '0.875rem', fontWeight: 500, color: 'var(--text-secondary)' }}>Status</label>
          <select value={form.status} onChange={set('status')} style={{ padding: '11px 14px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: '0.9375rem', background: 'var(--bg-card)', outline: 'none' }}>
            {['ACTIVE', 'INACTIVE', 'SUSPENDED'].map((s) => <option key={s}>{s}</option>)}
          </select>
        </div>
        <Input label="New Password (leave blank to keep)" type="password" placeholder="••••••••" value={form.password} onChange={set('password')} />
        {isDoctor && (
          <>
            <Input label="Specialization" value={form.specialization} onChange={set('specialization')} placeholder="e.g. Cardiology" />
            <Input label="License Number" value={form.licenseNumber} onChange={set('licenseNumber')} placeholder="e.g. LIC-12345" />
          </>
        )}
        <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end', marginTop: 8 }}>
          <Button variant="ghost" onClick={onClose}>Cancel</Button>
          <Button loading={loading} onClick={handleSave}>Save Changes</Button>
        </div>
      </div>
    </Modal>
  );
}

function RoleModal({ user, open, onClose, onSaved }) {
  const [role, setRole] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (user) setRole(user.roles?.[0]?.name || user.roles?.[0] || 'PATIENT');
  }, [user]);

  const handleSave = async () => {
    setLoading(true);
    try {
      await adminApi.changeUserRole(user.id, role);
      toast.success('Role updated');
      onSaved();
      onClose();
    } catch {
      toast.error('Failed to update role');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Modal open={open} onClose={onClose} title={`Change Role — ${user?.username}`} size="sm">
      <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
        <p style={{ color: 'var(--text-secondary)', fontSize: '0.9rem' }}>Select the new role for this user.</p>
        <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
          {ROLES.map((r) => (
            <label key={r} style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '12px 14px', border: `1.5px solid ${role === r ? 'var(--primary)' : 'var(--border)'}`, borderRadius: 'var(--radius-sm)', cursor: 'pointer', background: role === r ? 'rgba(10,110,97,0.05)' : 'transparent', transition: 'all 0.15s' }}>
              <input type="radio" name="role" value={r} checked={role === r} onChange={() => setRole(r)} style={{ accentColor: 'var(--primary)' }} />
              <Badge label={r} />
            </label>
          ))}
        </div>
        <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end' }}>
          <Button variant="ghost" onClick={onClose}>Cancel</Button>
          <Button loading={loading} onClick={handleSave}>Update Role</Button>
        </div>
      </div>
    </Modal>
  );
}

export default function UserManagementPage() {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState('ALL');
  const [editUser, setEditUser] = useState(null);
  const [roleUser, setRoleUser] = useState(null);
  const [deleteConfirm, setDeleteConfirm] = useState(null);

  const fetchUsers = useCallback(() => {
    setLoading(true);
    adminApi.getAllUsers()
      .then((res) => setUsers(res.data))
      .catch(() => toast.error('Failed to load users'))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => { fetchUsers(); }, [fetchUsers]);

  const handleDelete = async (user) => {
    try {
      await adminApi.deleteUser(user.id);
      toast.success(`${user.username} deleted`);
      setDeleteConfirm(null);
      fetchUsers();
    } catch {
      toast.error('Delete failed');
    }
  };

  const filtered = users.filter((u) => {
    const matchSearch = u.username?.toLowerCase().includes(search.toLowerCase()) ||
      u.email?.toLowerCase().includes(search.toLowerCase());
    const matchRole = roleFilter === 'ALL' || u.roles?.some((r) => (r.name || r) === roleFilter);
    return matchSearch && matchRole;
  });

  return (
    <DashboardLayout>
      <div style={{ maxWidth: 1200, animation: 'fadeIn 0.3s ease' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 28 }}>
          <div>
            <h1 style={{ fontSize: '1.75rem', fontWeight: 700 }}>User Management</h1>
            <p style={{ color: 'var(--text-secondary)', marginTop: 4 }}>{users.length} total users registered</p>
          </div>
          <Button icon={<RefreshCw size={15} />} variant="ghost" onClick={fetchUsers}>Refresh</Button>
        </div>

        {/* Filters */}
        <div style={{ display: 'flex', gap: 12, marginBottom: 20, flexWrap: 'wrap' }}>
          <div style={{ position: 'relative', flex: '1 1 260px' }}>
            <Search size={16} style={{ position: 'absolute', left: 12, top: '50%', transform: 'translateY(-50%)', color: 'var(--text-muted)' }} />
            <input
              placeholder="Search by username or email..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              style={{ width: '100%', padding: '10px 14px 10px 38px', border: '1.5px solid var(--border)', borderRadius: 'var(--radius-sm)', fontSize: '0.9375rem', background: 'var(--bg-card)', outline: 'none', color: 'var(--text-primary)' }}
            />
          </div>
          <div style={{ display: 'flex', gap: 8 }}>
            {['ALL', 'PATIENT', 'DOCTOR', 'ADMIN'].map((r) => (
              <button key={r} onClick={() => setRoleFilter(r)} style={{
                padding: '9px 16px', borderRadius: 'var(--radius-sm)', fontSize: '0.8125rem', fontWeight: 500, cursor: 'pointer',
                border: '1.5px solid',
                borderColor: roleFilter === r ? 'var(--primary)' : 'var(--border)',
                background: roleFilter === r ? 'var(--primary)' : 'var(--bg-card)',
                color: roleFilter === r ? '#fff' : 'var(--text-secondary)',
                transition: 'all 0.15s',
              }}>
                {r === 'ALL' ? 'All' : r}
              </button>
            ))}
          </div>
        </div>

        <Card>
          {loading ? (
            <div style={{ textAlign: 'center', padding: 48, color: 'var(--text-muted)' }}>
              <div style={{ width: 32, height: 32, border: '3px solid var(--border)', borderTopColor: 'var(--primary)', borderRadius: '50%', animation: 'spin 0.8s linear infinite', margin: '0 auto 12px' }} />
              Loading users...
            </div>
          ) : filtered.length === 0 ? (
            <div style={{ textAlign: 'center', padding: 48, color: 'var(--text-muted)' }}>No users found.</div>
          ) : (
            <div style={{ overflowX: 'auto' }}>
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr style={{ borderBottom: '2px solid var(--border)' }}>
                    {['User', 'Email', 'Phone', 'Role', 'Status', 'Approved', 'Actions'].map((h) => (
                      <th key={h} style={{ padding: '10px 14px', textAlign: 'left', fontSize: '0.75rem', fontWeight: 600, color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em', whiteSpace: 'nowrap' }}>{h}</th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {filtered.map((u) => (
                    <tr key={u.id} style={{ borderBottom: '1px solid var(--border)', transition: 'background 0.12s' }}
                      onMouseEnter={e => e.currentTarget.style.background = 'var(--bg)'}
                      onMouseLeave={e => e.currentTarget.style.background = 'transparent'}
                    >
                      <td style={{ padding: '13px 14px' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                          <div style={{ width: 34, height: 34, borderRadius: '50%', background: 'var(--primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontSize: '0.8125rem', fontWeight: 700, flexShrink: 0 }}>
                            {u.username?.[0]?.toUpperCase()}
                          </div>
                          <div>
                            <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>{u.username}</div>
                            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>ID: {u.id}</div>
                          </div>
                        </div>
                      </td>
                      <td style={{ padding: '13px 14px', color: 'var(--text-secondary)', fontSize: '0.875rem' }}>{u.email}</td>
                      <td style={{ padding: '13px 14px', color: 'var(--text-secondary)', fontSize: '0.875rem' }}>{u.phoneNumber || '—'}</td>
                      <td style={{ padding: '13px 14px' }}>
                        <div style={{ display: 'flex', gap: 4, flexWrap: 'wrap' }}>
                          {(u.roles || []).map((r) => <Badge key={r.id || r.name || r} label={r.name || r} />)}
                        </div>
                      </td>
                      <td style={{ padding: '13px 14px' }}><Badge label={u.status || 'ACTIVE'} /></td>
                      <td style={{ padding: '13px 14px' }}>
                        <Badge label={u.approved ? 'Yes' : 'No'} variant={u.approved ? 'APPROVED' : 'PENDING'} />
                      </td>
                      <td style={{ padding: '13px 14px' }}>
                        <div style={{ display: 'flex', gap: 6 }}>
                          <button onClick={() => setEditUser(u)} title="Edit" style={{ padding: 7, borderRadius: 6, border: '1px solid var(--border)', background: 'var(--bg-card)', cursor: 'pointer', color: 'var(--text-secondary)', display: 'flex', transition: 'all 0.15s' }}
                            onMouseEnter={e => { e.currentTarget.style.background = 'var(--primary)'; e.currentTarget.style.color = '#fff'; e.currentTarget.style.borderColor = 'var(--primary)'; }}
                            onMouseLeave={e => { e.currentTarget.style.background = 'var(--bg-card)'; e.currentTarget.style.color = 'var(--text-secondary)'; e.currentTarget.style.borderColor = 'var(--border)'; }}
                          ><Edit2 size={14} /></button>
                          <button onClick={() => setRoleUser(u)} title="Change Role" style={{ padding: 7, borderRadius: 6, border: '1px solid var(--border)', background: 'var(--bg-card)', cursor: 'pointer', color: 'var(--text-secondary)', display: 'flex', transition: 'all 0.15s' }}
                            onMouseEnter={e => { e.currentTarget.style.background = '#1D4ED8'; e.currentTarget.style.color = '#fff'; e.currentTarget.style.borderColor = '#1D4ED8'; }}
                            onMouseLeave={e => { e.currentTarget.style.background = 'var(--bg-card)'; e.currentTarget.style.color = 'var(--text-secondary)'; e.currentTarget.style.borderColor = 'var(--border)'; }}
                          ><Shield size={14} /></button>
                          <button onClick={() => setDeleteConfirm(u)} title="Delete" style={{ padding: 7, borderRadius: 6, border: '1px solid var(--border)', background: 'var(--bg-card)', cursor: 'pointer', color: 'var(--text-secondary)', display: 'flex', transition: 'all 0.15s' }}
                            onMouseEnter={e => { e.currentTarget.style.background = 'var(--danger)'; e.currentTarget.style.color = '#fff'; e.currentTarget.style.borderColor = 'var(--danger)'; }}
                            onMouseLeave={e => { e.currentTarget.style.background = 'var(--bg-card)'; e.currentTarget.style.color = 'var(--text-secondary)'; e.currentTarget.style.borderColor = 'var(--border)'; }}
                          ><Trash2 size={14} /></button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </Card>

        {/* Edit Modal */}
        <EditUserModal user={editUser} open={!!editUser} onClose={() => setEditUser(null)} onSaved={fetchUsers} />

        {/* Role Modal */}
        <RoleModal user={roleUser} open={!!roleUser} onClose={() => setRoleUser(null)} onSaved={fetchUsers} />

        {/* Delete Confirm Modal */}
        <Modal open={!!deleteConfirm} onClose={() => setDeleteConfirm(null)} title="Confirm Delete" size="sm">
          <p style={{ color: 'var(--text-secondary)', marginBottom: 24 }}>
            Are you sure you want to permanently delete <strong style={{ color: 'var(--text-primary)' }}>{deleteConfirm?.username}</strong>? This action cannot be undone.
          </p>
          <div style={{ display: 'flex', gap: 10, justifyContent: 'flex-end' }}>
            <Button variant="ghost" onClick={() => setDeleteConfirm(null)}>Cancel</Button>
            <Button variant="danger" onClick={() => handleDelete(deleteConfirm)}>Delete User</Button>
          </div>
        </Modal>
      </div>
    </DashboardLayout>
  );
}
