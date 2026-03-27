package com.healthcare.patient_service.dto;

import lombok.Data;

@Data
public class MedicalReportResponse {
    private Long id;
    private Long patientId;
    private String fileName;
    private String filePath;
}