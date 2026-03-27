package com.healthcare.patient_service.dto;

import lombok.Data;

@Data
public class PrescriptionResponse {
    private Long id;
    private Long patientId;
    private String doctorId;
    private String medications;
    private String notes;
}