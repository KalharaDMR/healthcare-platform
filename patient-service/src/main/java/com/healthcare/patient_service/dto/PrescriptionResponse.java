package com.healthcare.patient_service.dto;

import lombok.Data;

@Data
public class PrescriptionResponse {
    private Long id;
    private Long userId;     // Changed to Long
    private String doctorId;
    private String medications;
    private String notes;
}