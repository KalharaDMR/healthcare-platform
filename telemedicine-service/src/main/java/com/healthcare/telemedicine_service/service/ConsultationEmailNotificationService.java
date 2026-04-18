package com.healthcare.telemedicine_service.service;

import com.healthcare.telemedicine_service.client.AppointmentServiceClient;
import com.healthcare.telemedicine_service.client.AuthServiceClient;
import com.healthcare.telemedicine_service.client.AuthUserSummary;
import com.healthcare.telemedicine_service.client.NotificationSendRequest;
import com.healthcare.telemedicine_service.client.NotificationServiceClient;
import com.healthcare.telemedicine_service.dto.AppointmentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class ConsultationEmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(ConsultationEmailNotificationService.class);

    private final AppointmentServiceClient appointmentServiceClient;
    private final AuthServiceClient authServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    @Value("${internal.api.key}")
    private String internalApiKey;

    public ConsultationEmailNotificationService(AppointmentServiceClient appointmentServiceClient,
                                                AuthServiceClient authServiceClient,
                                                NotificationServiceClient notificationServiceClient) {
        this.appointmentServiceClient = appointmentServiceClient;
        this.authServiceClient = authServiceClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    @Async
    public void sendConsultationCompleted(Long appointmentId) {
        if (appointmentId == null) {
            return;
        }
        try {
            AppointmentResponse appt = appointmentServiceClient.getAppointmentById(appointmentId, internalApiKey);
            if (appt == null) {
                return;
            }

            AuthUserSummary patient = safeUser(appt.getPatientUsername());
            AuthUserSummary doctor = safeUser(appt.getDoctorUsername());

            String when = appt.getAppointmentDate() != null && appt.getStartTime() != null
                    ? appt.getAppointmentDate() + " " + appt.getStartTime() + " - " + appt.getEndTime()
                    : "scheduled time on file";

            if (patient != null && hasEmail(patient)) {
                String subject = "Consultation completed";
                String message = String.format(
                        "Hello,%n%nYour video consultation with Dr. %s (%s) for appointment #%d has ended.%n%nThank you for using Healthcare Platform.",
                        appt.getDoctorUsername(),
                        when,
                        appt.getId()
                );
                sendEmail(patient.getEmail(), subject, message, "CONSULTATION_COMPLETED");
            }

            if (doctor != null && hasEmail(doctor)) {
                String subject = "Consultation completed";
                String message = String.format(
                        "Hello,%n%nYour video consultation with patient %s (%s) for appointment #%d has ended.%n%n— Healthcare Platform",
                        appt.getPatientUsername(),
                        when,
                        appt.getId()
                );
                sendEmail(doctor.getEmail(), subject, message, "CONSULTATION_COMPLETED");
            }
        } catch (Exception e) {
            log.warn("Consultation completion email failed: {}", e.getMessage());
        }
    }

    private AuthUserSummary safeUser(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        try {
            return authServiceClient.getUserByUsername(username);
        } catch (Exception e) {
            log.warn("Could not resolve user {}: {}", username, e.getMessage());
            return null;
        }
    }

    private boolean hasEmail(AuthUserSummary user) {
        return user.getEmail() != null && !user.getEmail().isBlank();
    }

    private void sendEmail(String to, String subject, String message, String type) {
        NotificationSendRequest req = new NotificationSendRequest();
        req.setRecipient(to.trim());
        req.setSubject(subject);
        req.setMessage(message);
        req.setType(type);
        notificationServiceClient.send(req, internalApiKey);
    }
}