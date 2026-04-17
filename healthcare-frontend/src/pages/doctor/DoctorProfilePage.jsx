import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import { doctorApi } from '../../api/doctorApi';

export default function DoctorProfilePage() {
  const [profile, setProfile] = useState({
    doctorName: '',
    email: '',
    phoneNumber: '',
    specialization: '',
    location: '',
    licenseNumber: '',
  });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const loadProfile = async () => {
    setLoading(true);
    try {
      const res = await doctorApi.getProfile();
      setProfile((prev) => ({ ...prev, ...res.data }));
    } catch {
      toast.error('Failed to load profile');
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
      await doctorApi.updateProfile(profile);
      toast.success('Profile updated');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Unable to save profile');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div style={{ maxWidth: 900, animation: 'fadeIn 0.3s ease' }}>
      <div style={{ marginBottom: 24 }}>
        <h1 style={{ fontSize: '1.65rem', fontWeight: 700 }}>Professional profile</h1>
        <p style={{ color: 'var(--text-secondary)', marginTop: 6, maxWidth: 560 }}>
          Information patients and administrators see about you. Keep contact details current.
        </p>
      </div>

      <Card title="Details" action={<Button variant="ghost" onClick={loadProfile} loading={loading}>Refresh</Button>}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 12 }}>
          <Input label="Doctor name" value={profile.doctorName || ''} onChange={(e) => setProfile((p) => ({ ...p, doctorName: e.target.value }))} />
          <Input label="Email" type="email" value={profile.email || ''} onChange={(e) => setProfile((p) => ({ ...p, email: e.target.value }))} />
          <Input label="Phone" value={profile.phoneNumber || ''} onChange={(e) => setProfile((p) => ({ ...p, phoneNumber: e.target.value }))} />
          <Input label="Specialization" value={profile.specialization || ''} onChange={(e) => setProfile((p) => ({ ...p, specialization: e.target.value }))} />
          <Input label="Location" value={profile.location || ''} onChange={(e) => setProfile((p) => ({ ...p, location: e.target.value }))} />
          <Input label="License number" value={profile.licenseNumber || ''} onChange={(e) => setProfile((p) => ({ ...p, licenseNumber: e.target.value }))} />
        </div>
        <div style={{ marginTop: 16 }}>
          <Button onClick={saveProfile} loading={saving}>Save changes</Button>
        </div>
      </Card>
    </div>
  );
}
