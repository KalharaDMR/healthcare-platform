import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

export default function ProtectedRoute({ children, requiredRole }) {
  const { user, token, loading } = useAuth();
  const location = useLocation();

  if (loading) return <div>Loading...</div>; // 🔥 IMPORTANT

  if (!token || !user) {
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  if (requiredRole) {
    const roles = user?.roles || [];
    if (!roles.includes(requiredRole.toUpperCase())) {
      return <Navigate to="/unauthorized" replace />;
    }
  }

  return children;
}