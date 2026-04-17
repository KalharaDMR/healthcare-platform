import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import { patientApi } from '../../api/patientApi';

export default function PatientProfilePage() {
  const [profile, setProfile] = useState({
    username: '',
    fullName: '',
    email: '',
    phoneNumber: '',
    password: '',
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const loadProfile = async () => {
    setLoading(true);
    try {
      const res = await patientApi.getProfile();
      setProfile((prev) => ({
        ...prev,
        username: res.data?.username || '',
        fullName: res.data?.fullName || '',
        email: res.data?.email || '',
        phoneNumber: res.data?.phoneNumber || '',
        password: '',
      }));
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to load profile');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProfile();
  }, []);

  const saveProfile = async () => {
    setSaving(true);
    try {
      await patientApi.updateProfile({
        fullName: profile.fullName,
        email: profile.email,
        phoneNumber: profile.phoneNumber,
        password: profile.password || null,
      });
      setProfile((prev) => ({ ...prev, password: '' }));
      toast.success('Profile updated');
      loadProfile();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Unable to save profile');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div style={{ maxWidth: 900, animation: 'fadeIn 0.3s ease' }}>
      <div style={{ marginBottom: 24 }}>
        <h1 style={{ fontSize: '1.65rem', fontWeight: 700 }}>Manage profile</h1>
        <p style={{ color: 'var(--text-secondary)', marginTop: 6, maxWidth: 560 }}>
          Update your personal details and keep your account information current.
        </p>
      </div>

      <Card title="Profile details" action={<Button variant="ghost" onClick={loadProfile} loading={loading}>Refresh</Button>}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 12 }}>
          <Input label="Username" value={profile.username} disabled />
          <Input label="Full name" value={profile.fullName} onChange={(e) => setProfile((p) => ({ ...p, fullName: e.target.value }))} />
          <Input label="Email" type="email" value={profile.email} onChange={(e) => setProfile((p) => ({ ...p, email: e.target.value }))} />
          <Input label="Phone" value={profile.phoneNumber} onChange={(e) => setProfile((p) => ({ ...p, phoneNumber: e.target.value }))} />
          <Input
            label="New password"
            type="password"
            value={profile.password}
            onChange={(e) => setProfile((p) => ({ ...p, password: e.target.value }))}
            placeholder="Leave blank to keep current password"
          />
        </div>
        <div style={{ marginTop: 16 }}>
          <Button onClick={saveProfile} loading={saving}>Save changes</Button>
        </div>
      </Card>
    </div>
  );
}
