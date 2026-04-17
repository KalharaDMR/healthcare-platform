package com.healthcare.notification_service.dto;

import lombok.Data;

@Data
public class AppointmentNotificationEvent {
    private String patientEmail;
    private String doctorName;
    private String slotDateTime;
}