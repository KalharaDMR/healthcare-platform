import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import { doctorApi } from '../../api/doctorApi';
import { formatMoneyLineFromUsd, lkrToUsd } from '../../utils/currency';
import { useExchangeRate } from '../../context/ExchangeRateContext';

export default function DoctorAvailabilityPage() {
  const { usdToLkrRate, loading: fxLoading, usingFallback, refresh: refreshFx } = useExchangeRate();
  const [slots, setSlots] = useState([]);
  const [loading, setLoading] = useState(true);
  const [slotForm, setSlotForm] = useState({
    date: '',
    startTime: '',
    endTime: '',
    hospital: '',
    lkrVideoCost: '',
    lkrNormalCost: '',
  });
  const [creating, setCreating] = useState(false);

  const loadSlots = async () => {
    setLoading(true);
    try {
      const res = await doctorApi.getMyAvailability();
      setSlots(res.data || []);
    } catch {
      toast.error('Failed to load availability');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSlots();
  }, []);

  const createSlot = async () => {
    if (!slotForm.date || !slotForm.startTime || !slotForm.endTime || !slotForm.hospital) {
      toast.error('Fill date, times, and hospital');
      return;
    }
    setCreating(true);
    try {
      const payload = {
        date: slotForm.date,
        startTime: slotForm.startTime,
        endTime: slotForm.endTime,
        hospital: slotForm.hospital,
        available: true,
        costForTheVideoConferencingAppointment: lkrToUsd(slotForm.lkrVideoCost, usdToLkrRate),
        costForTheNormalAppointment: lkrToUsd(slotForm.lkrNormalCost, usdToLkrRate),
      };
      await doctorApi.createAvailability(payload);
      toast.success('Availability slot created');
      setSlotForm({ date: '', startTime: '', endTime: '', hospital: '', lkrVideoCost: '', lkrNormalCost: '' });
      loadSlots();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create slot');
    } finally {
      setCreating(false);
    }
  };

  const removeSlot = async (slotId) => {
    try {
      await doctorApi.deleteAvailability(slotId);
      toast.success('Slot removed');
      loadSlots();
    } catch {
      toast.error('Failed to remove slot');
    }
  };

  return (
    <div style={{ maxWidth: 960, animation: 'fadeIn 0.3s ease', display: 'grid', gap: 22 }}>
      <div>
        <h1 style={{ fontSize: '1.65rem', fontWeight: 700 }}>Availability</h1>
        <p style={{ color: 'var(--text-secondary)', marginTop: 6, maxWidth: 600 }}>
          Add bookable slots with fees in LKR; amounts are stored in USD on the server.
        </p>
        <p style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', marginTop: 8 }}>
          {fxLoading ? 'Loading rate…' : (
            <>
              1 USD ≈ {usdToLkrRate.toFixed(2)} LKR
              {usingFallback ? ' (fallback)' : ' (live)'}
              {' · '}
              <button type="button" onClick={() => refreshFx(true)} style={{ background: 'none', border: 'none', color: 'var(--primary)', cursor: 'pointer', font: 'inherit', textDecoration: 'underline' }}>Refresh rate</button>
            </>
          )}
        </p>
      </div>

      <Card title="Create a slot" subtitle={`Fees in LKR → USD using 1 USD ≈ ${fxLoading ? '…' : usdToLkrRate.toFixed(2)} LKR`}>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(150px, 1fr))', gap: 12 }}>
          <Input label="Date" type="date" value={slotForm.date} onChange={(e) => setSlotForm((s) => ({ ...s, date: e.target.value }))} />
          <Input label="Start" type="time" value={slotForm.startTime} onChange={(e) => setSlotForm((s) => ({ ...s, startTime: e.target.value }))} />
          <Input label="End" type="time" value={slotForm.endTime} onChange={(e) => setSlotForm((s) => ({ ...s, endTime: e.target.value }))} />
          <Input label="Hospital" value={slotForm.hospital} onChange={(e) => setSlotForm((s) => ({ ...s, hospital: e.target.value }))} />
          <Input label="Normal fee (LKR)" type="number" value={slotForm.lkrNormalCost} onChange={(e) => setSlotForm((s) => ({ ...s, lkrNormalCost: e.target.value }))} />
          <Input label="Video fee (LKR)" type="number" value={slotForm.lkrVideoCost} onChange={(e) => setSlotForm((s) => ({ ...s, lkrVideoCost: e.target.value }))} />
        </div>
        <div style={{ marginTop: 14 }}>
          <Button onClick={createSlot} loading={creating}>Add slot</Button>
        </div>
      </Card>

      <Card title="Your slots" subtitle="Stored as USD in the backend; shown with LKR reference" action={<Button variant="ghost" onClick={loadSlots} loading={loading}>Refresh</Button>}>
        {slots.length === 0 && !loading ? (
          <p style={{ color: 'var(--text-secondary)', margin: 0 }}>No slots yet. Create one above.</p>
        ) : (
          <div style={{ display: 'grid', gap: 10 }}>
            {slots.map((slot) => (
              <div key={slot.id} style={{ border: '1px solid var(--border)', borderRadius: 10, padding: 14, display: 'flex', justifyContent: 'space-between', alignItems: 'center', gap: 12, flexWrap: 'wrap' }}>
                <div>
                  <div style={{ fontWeight: 600 }}>{slot.date} · {slot.startTime} – {slot.endTime}</div>
                  <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginTop: 4 }}>
                    {slot.hospital} · In-person {formatMoneyLineFromUsd(slot.costForTheNormalAppointment, usdToLkrRate)}
                    {' · '}
                    Video {formatMoneyLineFromUsd(slot.costForTheVideoConferencingAppointment, usdToLkrRate)}
                  </div>
                </div>
                <Button variant="danger" onClick={() => removeSlot(slot.id)}>Delete</Button>
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  );
}
