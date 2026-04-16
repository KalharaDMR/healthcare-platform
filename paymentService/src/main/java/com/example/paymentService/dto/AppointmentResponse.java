package com.example.paymentService.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class AppointmentResponse {
    private Long id;
    private Long slotId;
    private String patientUsername;
    private String doctorUsername;
    private String hospital;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isVideoConferencingAppointment;
    private AppointmentStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
