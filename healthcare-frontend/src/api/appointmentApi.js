import api from './axiosInstance';

export const appointmentApi = {
  getMyAppointments: (status) => api.get('/api/appointments/my', { params: status ? { status } : {} }),
  getDoctorAppointments: (status) => api.get('/api/appointments/doctor', { params: status ? { status } : {} }),
  cancelMyAppointment: (appointmentId) => api.put(`/api/appointments/my/${appointmentId}/cancel`),
  updateDoctorAppointmentStatus: (appointmentId, status) =>
    api.put(`/api/appointments/doctor/${appointmentId}/status`, null, { params: { status } }),
};
