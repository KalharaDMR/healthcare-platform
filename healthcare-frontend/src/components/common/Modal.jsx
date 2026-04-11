import React, { useEffect } from 'react';
import { X } from 'lucide-react';

export default function Modal({ open, onClose, title, children, size = 'md' }) {
  useEffect(() => {
    if (open) document.body.style.overflow = 'hidden';
    else document.body.style.overflow = '';
    return () => { document.body.style.overflow = ''; };
  }, [open]);

  if (!open) return null;

  const widths = { sm: 420, md: 560, lg: 720, xl: 900 };

  return (
    <div
      onClick={onClose}
      style={{
        position: 'fixed', inset: 0,
        background: 'rgba(7, 46, 42, 0.45)',
        backdropFilter: 'blur(4px)',
        display: 'flex', alignItems: 'center', justifyContent: 'center',
        zIndex: 1000,
        padding: 16,
        animation: 'fadeIn 0.2s ease',
      }}
    >
      <div
        onClick={(e) => e.stopPropagation()}
        style={{
          background: 'var(--bg-card)',
          borderRadius: 'var(--radius-lg)',
          boxShadow: 'var(--shadow-lg)',
          width: '100%',
          maxWidth: widths[size],
          maxHeight: '90vh',
          overflow: 'auto',
          animation: 'fadeIn 0.25s ease',
        }}
      >
        <div style={{
          padding: '20px 24px',
          borderBottom: '1px solid var(--border)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          position: 'sticky', top: 0,
          background: 'var(--bg-card)',
          zIndex: 1,
        }}>
          <h2 style={{ fontSize: '1.125rem', fontWeight: 600 }}>{title}</h2>
          <button onClick={onClose} style={{
            background: 'none', border: 'none', cursor: 'pointer',
            color: 'var(--text-muted)', display: 'flex', alignItems: 'center',
            padding: 4, borderRadius: 6,
            transition: 'all 0.15s',
          }}
            onMouseEnter={e => e.currentTarget.style.background = 'var(--bg)'}
            onMouseLeave={e => e.currentTarget.style.background = 'none'}
          >
            <X size={20} />
          </button>
        </div>
        <div style={{ padding: '24px' }}>{children}</div>
      </div>
    </div>
  );
}
