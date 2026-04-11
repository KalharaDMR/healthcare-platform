import api from './axiosInstance';

export const adminApi = {
  // Users
  getAllUsers: () => api.get('/api/admin/users'),
  getUserById: (id) => api.get(`/api/admin/users/${id}`),
  updateUser: (id, data) => api.put(`/api/admin/users/${id}`, data),
  deleteUser: (id) => api.delete(`/api/admin/users/${id}`),
  changeUserRole: (id, roleName) => api.put(`/api/admin/users/${id}/role`, { roleName }),
  approveDoctor: (id) => api.put(`/api/admin/users/${id}/approve`),

  // Specializations
  getSpecializations: () => api.get('/api/admin/specializations'),
  addSpecialization: (name) => api.post('/api/admin/specializations', { name }),
};
