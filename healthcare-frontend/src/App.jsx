import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ExchangeRateProvider } from './context/ExchangeRateContext';
import ProtectedRoute from './components/common/ProtectedRoute';

// Auth pages
import LoginPage from './pages/auth/LoginPage';
import RegisterPage from './pages/auth/RegisterPage';
import DoctorRegisterPage from './pages/auth/DoctorRegisterPage';
import UnauthorizedPage from './pages/auth/UnauthorizedPage';
import HomePage from './pages/public/HomePage';

// Admin pages
import AdminDashboard from './pages/admin/AdminDashboard';
import UserManagementPage from './pages/admin/UserManagementPage';
import DoctorApprovalsPage from './pages/admin/DoctorApprovalsPage';
import SpecializationsPage from './pages/admin/SpecializationsPage';

// Role dashboards
import PatientShell from './pages/patient/PatientShell';
import PatientOverview from './pages/patient/PatientOverview';
import PatientDoctorsBrowse from './pages/patient/PatientDoctorsBrowse';
import PatientDoctorSlots from './pages/patient/PatientDoctorSlots';
import PatientAppointmentsPage from './pages/patient/PatientAppointmentsPage';
import PatientAiPage from './pages/patient/PatientAiPage';
import PatientProfilePage from './pages/patient/PatientProfilePage';
import DoctorShell from './pages/doctor/DoctorShell';
import DoctorOverview from './pages/doctor/DoctorOverview';
import DoctorProfilePage from './pages/doctor/DoctorProfilePage';
import DoctorAvailabilityPage from './pages/doctor/DoctorAvailabilityPage';
import DoctorAppointmentsPage from './pages/doctor/DoctorAppointmentsPage';
import DoctorClinicalPage from './pages/doctor/DoctorClinicalPage';
import TelemedicineRoomPage from './pages/TelemedicineRoomPage';
import PatientRecordsPage from './pages/patient/PatientRecordsPage';

import './styles/global.css';

function RootRedirect() {
  const { user, isAdmin, isDoctor } = useAuth();
  if (!user) return <Navigate to="/login" replace />;
  if (isAdmin) return <Navigate to="/admin/dashboard" replace />;
  if (isDoctor) return <Navigate to="/doctor/dashboard" replace />;
  return <Navigate to="/patient/dashboard" replace />;
}

export default function App() {
  return (
    <ExchangeRateProvider>
    <AuthProvider>
      <BrowserRouter>
        <Toaster
          position="top-right"
          toastOptions={{
            style: {
              borderRadius: '10px',
              fontFamily: 'DM Sans, sans-serif',
              fontSize: '0.9rem',
              boxShadow: '0 4px 20px rgba(0,0,0,0.12)',
            },
            success: { iconTheme: { primary: '#0A6E61', secondary: '#fff' } },
          }}
        />
        <Routes>
          {/* Public */}
          <Route path="/" element={<HomePage />} />
          <Route path="/dashboard" element={<RootRedirect />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/register/doctor" element={<DoctorRegisterPage />} />
          <Route path="/unauthorized" element={<UnauthorizedPage />} />

          {/* Admin */}
          <Route path="/admin/dashboard" element={<ProtectedRoute requiredRole="ADMIN"><AdminDashboard /></ProtectedRoute>} />
          <Route path="/admin/users" element={<ProtectedRoute requiredRole="ADMIN"><UserManagementPage /></ProtectedRoute>} />
          <Route path="/admin/doctors" element={<ProtectedRoute requiredRole="ADMIN"><DoctorApprovalsPage /></ProtectedRoute>} />
          <Route path="/admin/specializations" element={<ProtectedRoute requiredRole="ADMIN"><SpecializationsPage /></ProtectedRoute>} />

          {/* Doctor — sidebar shell + pages */}
          <Route
            path="/doctor"
            element={(
              <ProtectedRoute requiredRole="DOCTOR">
                <DoctorShell />
              </ProtectedRoute>
            )}
          >
            <Route index element={<Navigate to="/doctor/dashboard" replace />} />
            <Route path="dashboard" element={<DoctorOverview />} />
            <Route path="profile" element={<DoctorProfilePage />} />
            <Route path="availability" element={<DoctorAvailabilityPage />} />
            <Route path="appointments" element={<DoctorAppointmentsPage />} />
            <Route path="clinical" element={<DoctorClinicalPage />} />
            <Route path="consultation/:appointmentId" element={<TelemedicineRoomPage />} />
          </Route>

          {/* Patient — sidebar shell + section routes */}
          <Route
            path="/patient"
            element={(
              <ProtectedRoute requiredRole="PATIENT">
                <PatientShell />
              </ProtectedRoute>
            )}
          >
            <Route index element={<Navigate to="/patient/dashboard" replace />} />
            <Route path="dashboard" element={<PatientOverview />} />
            <Route path="profile" element={<PatientProfilePage />} />
            <Route path="doctors" element={<PatientDoctorsBrowse />} />
            <Route path="doctors/:username/slots" element={<PatientDoctorSlots />} />
            <Route path="appointments" element={<PatientAppointmentsPage />} />
            <Route path="records" element={<PatientRecordsPage />} />
            <Route path="ai" element={<PatientAiPage />} />
            <Route path="consultation/:appointmentId" element={<TelemedicineRoomPage />} />
          </Route>

          {/* Fallback */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
    </ExchangeRateProvider>
  );
}
