package com.example.paymentService.dto;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class Appointment {
    private String doctorUsername;

    private LocalDate date;

    private LocalTime startTime;

    private LocalTime endTime;

    private String hospital;

    private String patientUserName;

    private Boolean IsEnableVideoConferencing;
}
