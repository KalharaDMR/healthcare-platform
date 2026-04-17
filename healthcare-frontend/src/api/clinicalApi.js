import api from './axiosInstance';

export const clinicalApi = {
  issuePrescription: (payload) => api.post('/api/doctor/clinical/prescriptions', payload),
  getPatientHistoryByUsername: (patientUsername) =>
    api.get(`/api/doctor/clinical/patients/by-username/${encodeURIComponent(patientUsername)}/medical-history`),
  getMyPrescriptions: () => api.get('/api/prescriptions/me'),
  getMyReports: () => api.get('/api/medical-reports/me'),
  uploadMyReport: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    // Do not set Content-Type manually: axios default is application/json and
    // multipart/form-data without a boundary breaks Spring (400). Let the runtime set boundary.
    return api.post('/api/medical-reports/upload/me', formData, {
      transformRequest: [(data, headers) => {
        if (headers && typeof headers.delete === 'function') {
          headers.delete('Content-Type');
        } else if (headers) {
          delete headers['Content-Type'];
        }
        return data;
      }],
    });
  },
  getMyReportContent: (reportId) =>
    api.get(`/api/medical-reports/${reportId}/content/me`, { responseType: 'blob' }),
  getPatientReportContent: (reportId) =>
    api.get(`/api/medical-reports/${reportId}/content`, { responseType: 'blob' }),
};
