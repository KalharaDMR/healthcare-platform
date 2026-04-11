import React, { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard, Users, UserCheck, Stethoscope, Settings,
  LogOut, ChevronLeft, Menu, Heart, Bell, ChevronDown
} from 'lucide-react';
import { useAuth } from '../../context/AuthContext';

function NavItem({ to, icon, label, collapsed }) {
  return (
    <NavLink
      to={to}
      style={({ isActive }) => ({
        display: 'flex',
        alignItems: 'center',
        gap: 12,
        padding: collapsed ? '11px 16px' : '11px 16px',
        borderRadius: 'var(--radius-sm)',
        color: isActive ? '#fff' : 'var(--text-on-dark-muted)',
        background: isActive ? 'rgba(255,255,255,0.12)' : 'transparent',
        fontWeight: isActive ? 600 : 400,
        fontSize: '0.9375rem',
        textDecoration: 'none',
        transition: 'all 0.15s',
        whiteSpace: 'nowrap',
        overflow: 'hidden',
        justifyContent: collapsed ? 'center' : 'flex-start',
      })}
      onMouseEnter={e => {
        if (!e.currentTarget.style.background.includes('0.12')) {
          e.currentTarget.style.background = 'rgba(255,255,255,0.07)';
          e.currentTarget.style.color = '#fff';
        }
      }}
      onMouseLeave={e => {
        if (!e.currentTarget.style.background.includes('0.12')) {
          e.currentTarget.style.background = 'transparent';
          e.currentTarget.style.color = 'var(--text-on-dark-muted)';
        }
      }}
    >
      <span style={{ flexShrink: 0, display: 'flex' }}>{icon}</span>
      {!collapsed && <span>{label}</span>}
    </NavLink>
  );
}

export default function Sidebar({ collapsed, onToggle }) {
  const { user, logout, isAdmin, isDoctor, isPatient } = useAuth();
  const navigate = useNavigate();

  const adminLinks = [
    { to: '/admin/dashboard', icon: <LayoutDashboard size={18} />, label: 'Dashboard' },
    { to: '/admin/users', icon: <Users size={18} />, label: 'User Management' },
    { to: '/admin/doctors', icon: <Stethoscope size={18} />, label: 'Doctor Approvals' },
    { to: '/admin/specializations', icon: <Settings size={18} />, label: 'Specializations' },
  ];

  const doctorLinks = [
    { to: '/doctor/dashboard', icon: <LayoutDashboard size={18} />, label: 'Dashboard' },
  ];

  const patientLinks = [
    { to: '/patient/dashboard', icon: <LayoutDashboard size={18} />, label: 'Dashboard' },
  ];

  const links = isAdmin ? adminLinks : isDoctor ? doctorLinks : patientLinks;

  return (
    <aside style={{
      width: collapsed ? 64 : 'var(--sidebar-width)',
      background: 'var(--bg-sidebar)',
      height: '100vh',
      position: 'fixed',
      left: 0, top: 0,
      display: 'flex',
      flexDirection: 'column',
      transition: 'width 0.25s cubic-bezier(0.4,0,0.2,1)',
      zIndex: 100,
      overflow: 'hidden',
    }}>
      {/* Logo */}
      <div style={{
        padding: '20px 16px',
        display: 'flex',
        alignItems: 'center',
        gap: 10,
        borderBottom: '1px solid rgba(255,255,255,0.08)',
        justifyContent: collapsed ? 'center' : 'space-between',
      }}>
        {!collapsed && (
          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <div style={{
              width: 34, height: 34, borderRadius: 10,
              background: 'var(--primary-light)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
            }}>
              <Heart size={18} color="#fff" fill="#fff" />
            </div>
            <div>
              <div style={{ color: '#fff', fontFamily: 'Sora, sans-serif', fontWeight: 700, fontSize: '1rem', lineHeight: 1.2 }}>
                MediCare
              </div>
              <div style={{ color: 'var(--text-on-dark-muted)', fontSize: '0.7rem', letterSpacing: '0.06em' }}>
                HEALTH PLATFORM
              </div>
            </div>
          </div>
        )}
        {collapsed && (
          <div style={{
            width: 34, height: 34, borderRadius: 10,
            background: 'var(--primary-light)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
          }}>
            <Heart size={18} color="#fff" fill="#fff" />
          </div>
        )}
        <button onClick={onToggle} style={{
          background: 'rgba(255,255,255,0.08)',
          border: 'none', cursor: 'pointer',
          color: 'var(--text-on-dark-muted)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          padding: 6, borderRadius: 6, flexShrink: 0,
          transition: 'all 0.15s',
        }}
          onMouseEnter={e => e.currentTarget.style.background = 'rgba(255,255,255,0.15)'}
          onMouseLeave={e => e.currentTarget.style.background = 'rgba(255,255,255,0.08)'}
        >
          {collapsed ? <Menu size={16} /> : <ChevronLeft size={16} />}
        </button>
      </div>

      {/* Role badge */}
      {!collapsed && (
        <div style={{ padding: '12px 16px 8px' }}>
          <span style={{
            fontSize: '0.7rem',
            fontWeight: 600,
            letterSpacing: '0.1em',
            color: 'var(--accent)',
            textTransform: 'uppercase',
          }}>
            {isAdmin ? '⬡ Admin Portal' : isDoctor ? '⬡ Doctor Portal' : '⬡ Patient Portal'}
          </span>
        </div>
      )}

      {/* Nav links */}
      <nav style={{ flex: 1, padding: '8px 10px', display: 'flex', flexDirection: 'column', gap: 2 }}>
        {links.map((link) => (
          <NavItem key={link.to} {...link} collapsed={collapsed} />
        ))}
      </nav>

      {/* User section */}
      <div style={{
        padding: '12px 10px',
        borderTop: '1px solid rgba(255,255,255,0.08)',
      }}>
        {!collapsed && (
          <div style={{
            padding: '10px 12px',
            borderRadius: 'var(--radius-sm)',
            background: 'rgba(255,255,255,0.06)',
            marginBottom: 8,
            display: 'flex',
            alignItems: 'center',
            gap: 10,
          }}>
            <div style={{
              width: 36, height: 36, borderRadius: '50%',
              background: 'var(--primary)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              color: '#fff', fontWeight: 700, fontSize: '0.875rem',
              flexShrink: 0,
            }}>
              {user?.username?.[0]?.toUpperCase() || 'U'}
            </div>
            <div style={{ overflow: 'hidden' }}>
              <div style={{ color: '#fff', fontWeight: 600, fontSize: '0.875rem', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                {user?.username}
              </div>
              <div style={{ color: 'var(--text-on-dark-muted)', fontSize: '0.75rem' }}>
                {isAdmin ? 'Administrator' : isDoctor ? 'Doctor' : 'Patient'}
              </div>
            </div>
          </div>
        )}
        <button onClick={() => { logout(); navigate('/login'); }} style={{
          display: 'flex', alignItems: 'center', gap: 10,
          width: '100%', padding: '10px 12px',
          background: 'transparent', border: 'none', cursor: 'pointer',
          color: 'var(--text-on-dark-muted)',
          borderRadius: 'var(--radius-sm)',
          fontSize: '0.9375rem',
          transition: 'all 0.15s',
          justifyContent: collapsed ? 'center' : 'flex-start',
        }}
          onMouseEnter={e => {
            e.currentTarget.style.background = 'rgba(232,72,85,0.15)';
            e.currentTarget.style.color = '#E84855';
          }}
          onMouseLeave={e => {
            e.currentTarget.style.background = 'transparent';
            e.currentTarget.style.color = 'var(--text-on-dark-muted)';
          }}
        >
          <LogOut size={18} />
          {!collapsed && 'Sign Out'}
        </button>
      </div>
    </aside>
  );
}
