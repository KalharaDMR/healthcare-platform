package com.healthcare.telemedicine_service.service;

import com.healthcare.telemedicine_service.client.AppointmentServiceClient;
import com.healthcare.telemedicine_service.dto.AppointmentResponse;
import com.healthcare.telemedicine_service.dto.CreateSessionRequest;
import com.healthcare.telemedicine_service.dto.JoinSessionResponse;
import com.healthcare.telemedicine_service.entity.SessionStatus;
import com.healthcare.telemedicine_service.entity.VideoSession;
import com.healthcare.telemedicine_service.repository.VideoSessionRepository;
import com.healthcare.telemedicine_service.util.AgoraTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TelemedicineService {

    private final VideoSessionRepository sessionRepository;
    private final AppointmentServiceClient appointmentClient;
    private final AgoraTokenUtil tokenUtil;

    @Value("${internal.api.key}")
    private String internalApiKey;

    public TelemedicineService(VideoSessionRepository sessionRepository,
                               AppointmentServiceClient appointmentClient,
                               AgoraTokenUtil tokenUtil) {
        this.sessionRepository = sessionRepository;
        this.appointmentClient = appointmentClient;
        this.tokenUtil = tokenUtil;
    }

    @Transactional
    public JoinSessionResponse createSession(String username, String role, CreateSessionRequest request) {
        Long appointmentId = request.getAppointmentId();

        // Verify appointment via appointment service
        AppointmentResponse appointment = appointmentClient.getAppointmentById(appointmentId, internalApiKey);
        if (appointment == null) {
            throw new RuntimeException("Appointment not found");
        }

        // Check if the user is allowed to create session (only patient or doctor of that appointment)
        boolean isPatient = appointment.getPatientUsername().equals(username);
        boolean isDoctor = appointment.getDoctorUsername().equals(username);
        if (!isPatient && !isDoctor) {
            throw new RuntimeException("You are not authorized for this appointment");
        }

        // Check if session already exists
        VideoSession existing = sessionRepository.findByAppointmentId(appointmentId).orElse(null);
        if (existing != null) {
            // If session exists, return the token for the requester
            String token = (role.equals("PATIENT")) ? existing.getPatientToken() : existing.getDoctorToken();
            JoinSessionResponse response = new JoinSessionResponse();
            response.setChannelName(existing.getChannelName());
            response.setToken(token);
            response.setAppointmentId(appointmentId);
            return response;
        }

        // Generate unique channel name
        String channelName = "appt_" + appointmentId + "_" + System.currentTimeMillis();

        // Generate tokens for patient and doctor (uid = 0 for simplicity)
        int uid = 0;
        String patientToken = tokenUtil.generateToken(channelName, uid, 1); // publisher
        String doctorToken = tokenUtil.generateToken(channelName, uid, 1);   // publisher

        // Save session
        VideoSession session = new VideoSession();
        session.setAppointmentId(appointmentId);
        session.setChannelName(channelName);
        session.setPatientUsername(appointment.getPatientUsername());
        session.setDoctorUsername(appointment.getDoctorUsername());
        session.setPatientToken(patientToken);
        session.setDoctorToken(doctorToken);
        session.setStatus(SessionStatus.CREATED);
        sessionRepository.save(session);

        // Return the token for the requester
        String tokenForRequester = (role.equals("PATIENT")) ? patientToken : doctorToken;
        JoinSessionResponse response = new JoinSessionResponse();
        response.setChannelName(channelName);
        response.setToken(tokenForRequester);
        response.setAppointmentId(appointmentId);
        return response;
    }

    @Transactional
    public JoinSessionResponse joinSession(String username, String role, Long appointmentId) {
        VideoSession session = sessionRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Verify authorization
        boolean isPatient = session.getPatientUsername().equals(username);
        boolean isDoctor = session.getDoctorUsername().equals(username);
        if (!isPatient && !isDoctor) {
            throw new RuntimeException("You are not authorized for this session");
        }

        // Update status to ACTIVE if first join
        if (session.getStatus() == SessionStatus.CREATED) {
            session.setStatus(SessionStatus.ACTIVE);
            sessionRepository.save(session);
        }

        String token = (role.equals("PATIENT")) ? session.getPatientToken() : session.getDoctorToken();
        JoinSessionResponse response = new JoinSessionResponse();
        response.setChannelName(session.getChannelName());
        response.setToken(token);
        response.setAppointmentId(appointmentId);
        return response;
    }

    @Transactional
    public void endSession(String username, String role, Long appointmentId) {
        VideoSession session = sessionRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // Only doctor or patient can end session (or admin)
        boolean isPatient = session.getPatientUsername().equals(username);
        boolean isDoctor = session.getDoctorUsername().equals(username);
        if (!isPatient && !isDoctor && !role.equals("ADMIN")) {
            throw new RuntimeException("You are not authorized to end this session");
        }

        session.setStatus(SessionStatus.COMPLETED);
        session.setEndedAt(java.time.LocalDateTime.now());
        sessionRepository.save(session);
    }
}