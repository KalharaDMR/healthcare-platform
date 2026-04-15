import axios from 'axios';

const BASE_URL = 'http://localhost:8089';

const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT + role header to every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  // roles stored as plain strings ["ADMIN", "DOCTOR", ...]
  const user = JSON.parse(localStorage.getItem('user') || '{}');
  if (user?.roles?.length) {
    config.headers['X-User-Role'] = user.roles.join(',');
  }
  return config;
});

// Redirect to login on 401
api.interceptors.response.use(
  (res) => res,
  (err) => {
    const url = err.config?.url || '';

    // ❗ Only redirect if NOT public auth endpoints
    if (
      err.response?.status === 401 &&
      !url.includes('/api/auth') &&
      !url.includes('/api/admin/specializations') // 👈 ADD THIS
    ) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }

    return Promise.reject(err);
  }
);

export default api;
