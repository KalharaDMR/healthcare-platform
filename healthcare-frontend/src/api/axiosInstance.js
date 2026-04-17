import axios from 'axios';

//const BASE_URL = 'http://localhost:8089';
const BASE_URL = 'http://healthcare.local';

const api = axios.create({
  baseURL: BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

// Attach JWT to every request (identity headers are added by API Gateway)
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
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
