import api from './axiosInstance';

export const patientApi = {
  getProfile: () => api.get('/api/auth/profile/patient'),
  updateProfile: (payload) => api.put('/api/auth/profile/patient', payload),
};
