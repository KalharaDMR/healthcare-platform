import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import { ArrowLeft, Calendar, Stethoscope } from 'lucide-react';
import toast from 'react-hot-toast';
import Card from '../../components/common/Card';
import Button from '../../components/common/Button';
import Input from '../../components/common/Input';
import PaginationBar from '../../components/common/PaginationBar';
import { doctorApi } from '../../api/doctorApi';
import { paymentApi } from '../../api/paymentApi';
import { formatMoneyLineFromUsd } from '../../utils/currency';
import { useExchangeRate } from '../../context/ExchangeRateContext';
import StripePaymentModal from '../../components/payment/StripePaymentModal';

const SLOT_PAGE_SIZE = 6;

export default function PatientDoctorSlots() {
  const navigate = useNavigate();
  const { username: usernameParam } = useParams();
  const location = useLocation();
  const meta = location.state || {};
  const username = decodeURIComponent(usernameParam || '');

  const { usdToLkrRate, loading: fxLoading, usingFallback, refresh: refreshFx } = useExchangeRate();
  const [date, setDate] = useState('');
  const [allSlots, setAllSlots] = useState([]);
  const [slotLoading, setSlotLoading] = useState(true);
  const [page, setPage] = useState(1);
  /** While set, payment intent is being created — only this slot+mode shows the spinner; all book actions stay disabled. */
  const [pendingCharge, setPendingCharge] = useState(null);
  const [stripeClientSecret, setStripeClientSecret] = useState(null);
  const [stripeModalOpen, setStripeModalOpen] = useState(false);

  const loadSlots = useCallback(async () => {
    if (!username) return;
    setSlotLoading(true);
    try {
      const res = await doctorApi.getDoctorPublicAvailability(username, date ? { date } : {});
      setAllSlots(res.data || []);
      setPage(1);
    } catch {
      toast.error('Failed to load availability');
      setAllSlots([]);
    } finally {
      setSlotLoading(false);
    }
  }, [username, date]);

  useEffect(() => {
    loadSlots();
  }, [loadSlots]);

  const totalPages = Math.max(1, Math.ceil(allSlots.length / SLOT_PAGE_SIZE));
  const slots = useMemo(() => {
    const start = (page - 1) * SLOT_PAGE_SIZE;
    return allSlots.slice(start, start + SLOT_PAGE_SIZE);
  }, [allSlots, page]);

  const handlePayAndBook = async (slotId, isEnableVideoConferencing) => {
    setPendingCharge({ slotId, video: isEnableVideoConferencing });
    try {
      const res = await paymentApi.charge({ slotId, isEnableVideoConferencing });
      const secret = res.data?.client_secret;
      if (!secret) {
        toast.error('Server did not return a payment session. Check payment service logs.');
        return;
      }
      setStripeClientSecret(secret);
      setStripeModalOpen(true);
      toast.success('Secure payment form opened — enter your card details.');
    } catch (err) {
      toast.error(err.response?.data || 'Failed to start payment');
    } finally {
      setPendingCharge(null);
    }
  };

  const closeStripeModal = () => {
    setStripeModalOpen(false);
    setStripeClientSecret(null);
  };

  const handlePaymentSuccess = () => {
    closeStripeModal();
    toast.success('Payment completed.');
    navigate('/patient/appointments');
  };

  const displayName = meta.doctorName || username;

  return (
    <div style={{ maxWidth: 800, animation: 'fadeIn 0.3s ease' }}>
      <StripePaymentModal
        clientSecret={stripeClientSecret}
        open={stripeModalOpen}
        onClose={closeStripeModal}
        onSuccess={handlePaymentSuccess}
      />
      <div style={{ marginBottom: 20 }}>
        <Link
          to="/patient/doctors"
          style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: 8,
            color: 'var(--primary)',
            fontWeight: 600,
            fontSize: '0.9rem',
            textDecoration: 'none',
            marginBottom: 16,
          }}
        >
          <ArrowLeft size={18} />
          Back to all doctors
        </Link>
        <div
          style={{
            borderRadius: 'var(--radius-md)',
            padding: 20,
            border: '1px solid var(--border)',
            background: 'var(--bg-card)',
          }}
        >
          <h1 style={{ fontSize: '1.5rem', fontWeight: 700, marginBottom: 8 }}>{displayName}</h1>
          <div style={{ display: 'flex', flexWrap: 'wrap', gap: 16, fontSize: '0.9rem', color: 'var(--text-secondary)' }}>
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
              <Stethoscope size={16} />
              {meta.specialization || '—'}
            </span>
          </div>
          <p style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', marginTop: 12 }}>
            {fxLoading ? 'Loading USD→LKR rate…' : (
              <>
                1 USD ≈ {usdToLkrRate.toFixed(2)} LKR
                {usingFallback ? ' (offline fallback)' : ' (live)'}
                {' · '}
                <button
                  type="button"
                  onClick={() => refreshFx(true)}
                  style={{
                    background: 'none',
                    border: 'none',
                    color: 'var(--primary)',
                    cursor: 'pointer',
                    font: 'inherit',
                    textDecoration: 'underline',
                  }}
                >
                  Refresh rate
                </button>
              </>
            )}
          </p>
        </div>
      </div>

      <Card
        title="Available slots"
        subtitle="Filter by date, then book in-person or video. Prices show in LKR with USD reference."
      >
        <div style={{ display: 'flex', flexWrap: 'wrap', gap: 12, alignItems: 'flex-end', marginBottom: 8 }}>
          <div style={{ minWidth: 200 }}>
            <Input
              label="Filter by date (optional)"
              type="date"
              value={date}
              onChange={(e) => setDate(e.target.value)}
            />
          </div>
          <Button variant="secondary" onClick={() => loadSlots()} loading={slotLoading}>
            Refresh slots
          </Button>
        </div>

        {slotLoading ? (
          <p style={{ color: 'var(--text-secondary)' }}>Loading slots…</p>
        ) : allSlots.length === 0 ? (
          <div
            style={{
              border: '1px dashed var(--border)',
              borderRadius: 'var(--radius-md)',
              padding: 32,
              textAlign: 'center',
              color: 'var(--text-secondary)',
            }}
          >
            <Calendar size={36} style={{ marginBottom: 12, opacity: 0.5 }} />
            <p style={{ fontWeight: 600, marginBottom: 6 }}>No open slots</p>
            <p style={{ fontSize: '0.9rem' }}>
              Try another date or pick a different doctor from the directory.
            </p>
          </div>
        ) : (
          <>
            <div style={{ display: 'grid', gap: 12 }}>
              {slots.map((slot) => (
                <div
                  key={slot.id}
                  style={{
                    border: '1px solid var(--border)',
                    borderRadius: 10,
                    padding: 16,
                    background: 'var(--bg)',
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', gap: 12, flexWrap: 'wrap', alignItems: 'center' }}>
                    <div>
                      <div style={{ fontWeight: 600 }}>{slot.hospital}</div>
                      <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
                        {slot.date} · {slot.startTime} – {slot.endTime}
                      </div>
                      <div style={{ fontSize: '0.875rem', color: 'var(--text-secondary)', marginTop: 6 }}>
                        In-person: {formatMoneyLineFromUsd(slot.costForTheNormalAppointment, usdToLkrRate)}
                        {' · '}
                        Video: {formatMoneyLineFromUsd(slot.costForTheVideoConferencingAppointment, usdToLkrRate)}
                      </div>
                    </div>
                    <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                      <Button
                        onClick={() => handlePayAndBook(slot.id, false)}
                        loading={pendingCharge?.slotId === slot.id && pendingCharge.video === false}
                        disabled={pendingCharge !== null}
                      >
                        Book in-person
                      </Button>
                      <Button
                        variant="secondary"
                        onClick={() => handlePayAndBook(slot.id, true)}
                        loading={pendingCharge?.slotId === slot.id && pendingCharge.video === true}
                        disabled={pendingCharge !== null}
                      >
                        Book video
                      </Button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
            <PaginationBar
              page={page}
              totalPages={totalPages}
              totalItems={allSlots.length}
              pageSize={SLOT_PAGE_SIZE}
              onPageChange={setPage}
              itemLabel="slots"
            />
          </>
        )}
      </Card>
    </div>
  );
}
