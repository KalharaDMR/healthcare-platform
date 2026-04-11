import React, { useState } from 'react';
import Sidebar from './Sidebar';

export default function DashboardLayout({ children }) {
  const [collapsed, setCollapsed] = useState(false);
  const sidebarW = collapsed ? 64 : 256;

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: 'var(--bg)' }}>
      <Sidebar collapsed={collapsed} onToggle={() => setCollapsed((c) => !c)} />
      <main style={{
        marginLeft: sidebarW,
        flex: 1,
        padding: '32px',
        minHeight: '100vh',
        transition: 'margin-left 0.25s cubic-bezier(0.4,0,0.2,1)',
        maxWidth: `calc(100vw - ${sidebarW}px)`,
      }}>
        {children}
      </main>
    </div>
  );
}
