import api from './axiosInstance';

export const telemedicineApi = {
  createSession: (appointmentId) => api.post('/api/video/sessions', { appointmentId }),
  joinSession: (appointmentId) => api.post(`/api/video/sessions/${appointmentId}/join`),
  endSession: (appointmentId) => api.delete(`/api/video/sessions/${appointmentId}`),
};
