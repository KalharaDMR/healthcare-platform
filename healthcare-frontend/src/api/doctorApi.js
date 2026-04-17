import api from './axiosInstance';

export const doctorApi = {
  getProfile: () => api.get('/api/auth/profile/doctor'),
  updateProfile: (payload) => api.put('/api/auth/profile/doctor', payload),
  searchDoctors: (params) => api.get('/api/doctor/search', { params }),
  getDoctorPublicAvailability: (doctorUsername, params) =>
    api.get(`/api/doctor/availability/public/${doctorUsername}`, { params }),
  getMyAvailability: (params) => api.get('/api/doctor/availability', { params }),
  createAvailability: (payload) => api.post('/api/doctor/availability', payload),
  updateAvailability: (slotId, payload) => api.put(`/api/doctor/availability/${slotId}`, payload),
  deleteAvailability: (slotId) => api.delete(`/api/doctor/availability/${slotId}`),
};
