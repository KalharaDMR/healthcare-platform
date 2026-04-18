package com.healthcare.appointment.service;

import com.healthcare.appointment.client.AuthServiceClient;
import com.healthcare.appointment.client.AuthUserSummary;
import com.healthcare.appointment.client.NotificationSendRequest;
import com.healthcare.appointment.client.NotificationServiceClient;
import com.healthcare.appointment.dto.AppointmentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class AppointmentEmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentEmailNotificationService.class);

    private final AuthServiceClient authServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    @Value("${internal.api.key}")
    private String internalApiKey;

    public AppointmentEmailNotificationService(AuthServiceClient authServiceClient,
                                               NotificationServiceClient notificationServiceClient) {
        this.authServiceClient = authServiceClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    @Async
    public void sendBookingConfirmation(AppointmentResponse appt) {
        if (appt == null) {
            return;
        }
        try {
            AuthUserSummary patient = safeUser(appt.getPatientUsername());
            AuthUserSummary doctor = safeUser(appt.getDoctorUsername());

            String when = appt.getAppointmentDate() + " " + appt.getStartTime() + " - " + appt.getEndTime();
            String video = Boolean.TRUE.equals(appt.getIsVideoConferencingAppointment())
                    ? "Video consultation"
                    : "In-person visit";

            if (patient != null && hasEmail(patient)) {
                String subject = "Appointment confirmed";
                String message = String.format(
                        "Hello,%n%nYour appointment with Dr. %s at %s on %s (%s) is confirmed.%n%nAppointment ID: %d%n%n— Healthcare Platform",
                        appt.getDoctorUsername(),
                        appt.getHospital(),
                        when,
                        video,
                        appt.getId()
                );
                sendEmail(patient.getEmail(), subject, message, "APPOINTMENT_BOOKED");
            }

            if (doctor != null && hasEmail(doctor)) {
                String subject = "New appointment booked";
                String message = String.format(
                        "Hello,%n%nPatient %s booked an appointment at %s on %s (%s).%n%nAppointment ID: %d%n%n— Healthcare Platform",
                        appt.getPatientUsername(),
                        appt.getHospital(),
                        when,
                        video,
                        appt.getId()
                );
                sendEmail(doctor.getEmail(), subject, message, "APPOINTMENT_BOOKED");
            }
        } catch (Exception e) {
            log.warn("Booking confirmation email failed: {}", e.getMessage());
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