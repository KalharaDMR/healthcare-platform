import React, { useState } from 'react';
import { loadStripe } from '@stripe/stripe-js';
import { Elements, PaymentElement, useStripe, useElements } from '@stripe/react-stripe-js';
import Button from '../common/Button';

const publishableKey = process.env.REACT_APP_STRIPE_PUBLISHABLE_KEY;
const stripePromise = publishableKey ? loadStripe(publishableKey) : null;

function PaymentForm({ onSuccess, onClose }) {
  const stripe = useStripe();
  const elements = useElements();
  const [busy, setBusy] = useState(false);
  const [errorMessage, setErrorMessage] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!stripe || !elements) return;
    setBusy(true);
    setErrorMessage(null);
    const { error } = await stripe.confirmPayment({
      elements,
      confirmParams: {
        return_url: `${window.location.origin}/patient/appointments`,
      },
      redirect: 'if_required',
    });
    setBusy(false);
    if (error) {
      if (error.type === 'card_error' || error.type === 'validation_error') {
        setErrorMessage(error.message);
      } else {
        setErrorMessage(error.message || 'Payment could not be completed.');
      }
      return;
    }
    onSuccess?.();
  };

  return (
    <form onSubmit={handleSubmit} style={{ display: 'grid', gap: 16 }}>
      <PaymentElement />
      {errorMessage && (
        <p style={{ margin: 0, fontSize: '0.875rem', color: 'var(--danger)' }}>{errorMessage}</p>
      )}
      <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', justifyContent: 'flex-end' }}>
        <Button type="button" variant="ghost" onClick={onClose} disabled={busy}>
          Cancel
        </Button>
        <Button type="submit" loading={busy} disabled={!stripe || !elements}>
          Pay securely
        </Button>
      </div>
    </form>
  );
}

export default function StripePaymentModal({ clientSecret, open, onClose, onSuccess }) {
  if (!open || !clientSecret) return null;

  if (!stripePromise) {
    return (
      <div
        role="dialog"
        aria-modal="true"
        style={{
          position: 'fixed',
          inset: 0,
          zIndex: 2000,
          background: 'rgba(15,23,42,0.55)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          padding: 24,
        }}
      >
        <div
          style={{
            background: 'var(--bg-card)',
            borderRadius: 'var(--radius-md)',
            padding: 28,
            maxWidth: 440,
            border: '1px solid var(--border)',
            boxShadow: 'var(--shadow-lg)',
          }}
        >
          <h2 style={{ margin: '0 0 12px', fontSize: '1.125rem', fontWeight: 700 }}>Stripe not configured</h2>
          <p style={{ margin: 0, color: 'var(--text-secondary)', fontSize: '0.9rem', lineHeight: 1.5 }}>
            Add <code style={{ fontSize: '0.85em' }}>REACT_APP_STRIPE_PUBLISHABLE_KEY</code> to{' '}
            <code style={{ fontSize: '0.85em' }}>.env</code> in <code style={{ fontSize: '0.85em' }}>healthcare-frontend</code>{' '}
            (publishable key from the same Stripe account as your payment service secret key). Restart the dev server after saving.
          </p>
          <div style={{ marginTop: 20 }}>
            <Button variant="secondary" onClick={onClose}>Close</Button>
          </div>
        </div>
      </div>
    );
  }

  const options = {
    clientSecret,
    appearance: { theme: 'stripe' },
  };

  return (
    <div
      role="dialog"
      aria-modal="true"
      style={{
        position: 'fixed',
        inset: 0,
        zIndex: 2000,
        background: 'rgba(15,23,42,0.55)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 24,
        overflowY: 'auto',
      }}
      onClick={(e) => {
        if (e.target === e.currentTarget) onClose?.();
      }}
    >
      <div
        style={{
          background: 'var(--bg-card)',
          borderRadius: 'var(--radius-md)',
          padding: 28,
          maxWidth: 480,
          width: '100%',
          border: '1px solid var(--border)',
          boxShadow: 'var(--shadow-lg)',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <h2 style={{ margin: '0 0 8px', fontSize: '1.25rem', fontWeight: 700 }}>Complete payment</h2>
        <p style={{ margin: '0 0 20px', fontSize: '0.875rem', color: 'var(--text-secondary)' }}>
          Card details are processed by Stripe. We never store your full card number on our servers.
        </p>
        <Elements key={clientSecret} stripe={stripePromise} options={options}>
          <PaymentForm onSuccess={onSuccess} onClose={onClose} />
        </Elements>
      </div>
    </div>
  );
}
