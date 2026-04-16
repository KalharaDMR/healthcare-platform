import React from 'react';

export default function Card({ children, title, subtitle, action, padding = '24px', style, ...props }) {
  return (
    <div
      style={{
        background: 'var(--bg-card)',
        borderRadius: 'var(--radius-md)',
        boxShadow: 'var(--shadow-sm)',
        border: '1px solid var(--border)',
        overflow: 'hidden',
        ...style,
      }}
      {...props}
    >
      {(title || action) && (
        <div style={{
          padding: '16px 24px',
          borderBottom: '1px solid var(--border)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
        }}>
          <div>
            {title && <h3 style={{ fontSize: '1rem', fontWeight: 600, color: 'var(--text-primary)' }}>{title}</h3>}
            {subtitle && <p style={{ fontSize: '0.8125rem', color: 'var(--text-muted)', marginTop: 2 }}>{subtitle}</p>}
          </div>
          {action}
        </div>
      )}
      <div style={{ padding }}>{children}</div>
    </div>
  );
}
