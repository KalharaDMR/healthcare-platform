package com.healthcare.patient_service.service;

import com.healthcare.patient_service.client.AuthClient;
import com.healthcare.patient_service.dto.MedicalReportResponse;
import com.healthcare.patient_service.entity.MedicalReport;
import com.healthcare.patient_service.repository.MedicalReportRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicalReportService {

    private final MedicalReportRepository repository;
    private final AuthClient authClient;

    @Value("${internal.api.key}")
    private String apiKey;

    public MedicalReportService(MedicalReportRepository repository, AuthClient authClient) {
        this.repository = repository;
        this.authClient = authClient;
    }

    public MedicalReport save(MedicalReport report) {

        if (report.getUserId() != null) {
            authClient.getUserById(report.getUserId(), apiKey);
        }

        return repository.save(report);
    }

    public List<MedicalReport> getAll() {
        return repository.findAll();
    }

    public List<MedicalReport> getByUserId(Long userId) {

        authClient.getUserById(userId, apiKey);

        return repository.findByUserId(userId);
    }

    public MedicalReportResponse toResponse(MedicalReport report) {
        MedicalReportResponse dto = new MedicalReportResponse();
        dto.setId(report.getId());
        dto.setUserId(report.getUserId());
        dto.setFileName(report.getFileName());
        dto.setFilePath(report.getFilePath());
        return dto;
    }

    public List<MedicalReportResponse> toResponseList(List<MedicalReport> reports) {
        return reports.stream().map(this::toResponse).collect(Collectors.toList());
    }
}