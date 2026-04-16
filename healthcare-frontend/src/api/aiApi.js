import api from './axiosInstance';

export const aiApi = {
  analyzeSymptoms: (payload) => api.post('/api/ai/analyze', payload),
};
