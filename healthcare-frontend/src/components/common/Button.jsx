import React from 'react';

const variants = {
  primary: {
    background: 'var(--primary)',
    color: '#fff',
    border: 'none',
  },
  secondary: {
    background: 'transparent',
    color: 'var(--primary)',
    border: '1.5px solid var(--primary)',
  },
  danger: {
    background: 'var(--danger)',
    color: '#fff',
    border: 'none',
  },
  ghost: {
    background: 'transparent',
    color: 'var(--text-secondary)',
    border: '1.5px solid var(--border)',
  },
  success: {
    background: 'var(--success)',
    color: '#fff',
    border: 'none',
  },
};

export default function Button({
  children,
  variant = 'primary',
  size = 'md',
  loading = false,
  fullWidth = false,
  icon,
  ...props
}) {
  const sizeStyles = {
    sm: { padding: '6px 14px', fontSize: '0.8125rem', borderRadius: 'var(--radius-sm)' },
    md: { padding: '10px 20px', fontSize: '0.9375rem', borderRadius: 'var(--radius-sm)' },
    lg: { padding: '14px 28px', fontSize: '1rem', borderRadius: 'var(--radius-md)' },
  };

  return (
    <button
      {...props}
      disabled={loading || props.disabled}
      style={{
        ...variants[variant],
        ...sizeStyles[size],
        fontFamily: 'DM Sans, sans-serif',
        fontWeight: 500,
        display: 'inline-flex',
        alignItems: 'center',
        justifyContent: 'center',
        gap: '8px',
        cursor: loading || props.disabled ? 'not-allowed' : 'pointer',
        opacity: loading || props.disabled ? 0.65 : 1,
        transition: 'all 0.18s ease',
        width: fullWidth ? '100%' : undefined,
        whiteSpace: 'nowrap',
        ...props.style,
      }}
      onMouseEnter={(e) => {
        if (!loading && !props.disabled) {
          e.currentTarget.style.filter = 'brightness(1.1)';
          e.currentTarget.style.transform = 'translateY(-1px)';
          e.currentTarget.style.boxShadow = 'var(--shadow-md)';
        }
      }}
      onMouseLeave={(e) => {
        e.currentTarget.style.filter = '';
        e.currentTarget.style.transform = '';
        e.currentTarget.style.boxShadow = '';
      }}
    >
      {loading ? (
        <span style={{
          width: 16, height: 16,
          border: '2px solid currentColor',
          borderTopColor: 'transparent',
          borderRadius: '50%',
          display: 'inline-block',
          animation: 'spin 0.8s linear infinite',
        }} />
      ) : icon}
      {children}
    </button>
  );
}
