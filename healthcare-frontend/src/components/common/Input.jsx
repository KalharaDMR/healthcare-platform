import React, { useState } from 'react';

export default function Input({
  label,
  error,
  icon,
  rightIcon,
  hint,
  ...props
}) {
  const [focused, setFocused] = useState(false);

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
      {label && (
        <label style={{
          fontSize: '0.875rem',
          fontWeight: 500,
          color: error ? 'var(--danger)' : focused ? 'var(--primary)' : 'var(--text-secondary)',
          transition: 'color 0.2s',
        }}>
          {label}
        </label>
      )}
      <div style={{ position: 'relative', display: 'flex', alignItems: 'center' }}>
        {icon && (
          <span style={{
            position: 'absolute',
            left: 12,
            color: focused ? 'var(--primary)' : 'var(--text-muted)',
            display: 'flex',
            alignItems: 'center',
            transition: 'color 0.2s',
            pointerEvents: 'none',
          }}>
            {icon}
          </span>
        )}
        <input
          {...props}
          onFocus={(e) => { setFocused(true); props.onFocus?.(e); }}
          onBlur={(e) => { setFocused(false); props.onBlur?.(e); }}
          style={{
            width: '100%',
            padding: icon ? '11px 40px 11px 40px' : rightIcon ? '11px 40px 11px 14px' : '11px 14px',
            border: `1.5px solid ${error ? 'var(--danger)' : focused ? 'var(--primary)' : 'var(--border)'}`,
            borderRadius: 'var(--radius-sm)',
            fontSize: '0.9375rem',
            color: 'var(--text-primary)',
            background: 'var(--bg-card)',
            outline: 'none',
            transition: 'all 0.2s',
            boxShadow: focused ? `0 0 0 3px ${error ? 'rgba(232,72,85,0.12)' : 'rgba(10,110,97,0.12)'}` : 'none',
            ...props.style,
          }}
        />
        {rightIcon && (
          <span style={{
            position: 'absolute',
            right: 12,
            display: 'flex',
            alignItems: 'center',
            color: 'var(--text-muted)',
          }}>
            {rightIcon}
          </span>
        )}
      </div>
      {error && (
        <span style={{ fontSize: '0.8rem', color: 'var(--danger)' }}>{error}</span>
      )}
      {hint && !error && (
        <span style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{hint}</span>
      )}
    </div>
  );
}
