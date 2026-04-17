import api from './axiosInstance';

export const paymentApi = {
  charge: ({ slotId, isEnableVideoConferencing }) =>
    api.post(
      '/api/payments/charge',
      { slotId },
      { headers: { 'IS-enabled-video': String(Boolean(isEnableVideoConferencing)) } }
    ),
  patientRefund: ({ slotId, appointmentId }) =>
    api.post('/api/payments/refund', null, {
      params: { slotId, AppointmentId: appointmentId },
    }),
  doctorRefund: ({ slotId, appointmentId }) =>
    api.post('/api/payments/doctor-refund', null, {
      params: { slotId, appointmentId },
    }),
};
