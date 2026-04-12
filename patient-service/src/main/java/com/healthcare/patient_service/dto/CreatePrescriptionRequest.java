package com.healthcare.patient_service.dto;

import lombok.Data;

@Data
public class CreatePrescriptionRequest {
    private Long userId;
    private String doctorId;
    private String medications;
    private String notes;
}