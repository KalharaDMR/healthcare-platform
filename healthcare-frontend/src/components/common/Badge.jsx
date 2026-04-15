import React from 'react';

const presets = {
  ADMIN: { bg: '#EDE9FE', color: '#5B21B6' },
  DOCTOR: { bg: '#DBEAFE', color: '#1D4ED8' },
  PATIENT: { bg: '#D1FAE5', color: '#065F46' },
  ACTIVE: { bg: 'var(--success-light)', color: '#0D7A72' },
  INACTIVE: { bg: '#F3F4F6', color: '#6B7280' },
  SUSPENDED: { bg: 'var(--danger-light)', color: 'var(--danger)' },
  PENDING: { bg: 'var(--warning-light)', color: '#92400E' },
  APPROVED: { bg: 'var(--success-light)', color: '#065F46' },
  default: { bg: 'var(--bg)', color: 'var(--text-secondary)' },
};

export default function Badge({ label, variant }) {
  const style = presets[variant?.toUpperCase()] || presets[label?.toUpperCase()] || presets.default;
  return (
    <span style={{
      display: 'inline-flex',
      alignItems: 'center',
      padding: '3px 10px',
      borderRadius: '99px',
      fontSize: '0.75rem',
      fontWeight: 600,
      letterSpacing: '0.02em',
      background: style.bg,
      color: style.color,
      whiteSpace: 'nowrap',
    }}>
      {label}
    </span>
  );
}
