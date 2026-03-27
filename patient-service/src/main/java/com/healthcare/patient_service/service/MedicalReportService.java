package com.healthcare.patient_service.service;

import com.healthcare.patient_service.dto.MedicalReportResponse;
import com.healthcare.patient_service.entity.MedicalReport;
import com.healthcare.patient_service.repository.MedicalReportRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicalReportService {

    private final MedicalReportRepository repository;

    public MedicalReportService(MedicalReportRepository repository) {
        this.repository = repository;
    }

    // Save a new medical report
    public MedicalReport save(MedicalReport report) {
        return repository.save(report);
    }

    // Get all reports
    public List<MedicalReport> getAll() {
        return repository.findAll();
    }

    // Get report by ID
    public MedicalReport getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Medical report not found with ID: " + id));
    }

    // Get all reports for a patient
    public List<MedicalReport> getByPatientId(Long patientId) {
        return repository.findAll().stream()
                .filter(r -> r.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }

    // Convert MedicalReport entity → MedicalReportResponse DTO
    public MedicalReportResponse toResponse(MedicalReport report) {
        MedicalReportResponse dto = new MedicalReportResponse();
        dto.setId(report.getId());
        dto.setPatientId(report.getPatientId());
        dto.setFileName(report.getFileName());
        dto.setFilePath(report.getFilePath());
        return dto;
    }

    // Convert list of MedicalReport entities → list of DTOs
    public List<MedicalReportResponse> toResponseList(List<MedicalReport> reports) {
        return reports.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}