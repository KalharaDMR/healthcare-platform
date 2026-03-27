package com.healthcare.patient_service.controller;

import com.healthcare.patient_service.dto.CreateMedicalReportRequest;
import com.healthcare.patient_service.dto.MedicalReportResponse;
import com.healthcare.patient_service.entity.MedicalReport;
import com.healthcare.patient_service.service.MedicalReportService;
import com.healthcare.patient_service.service.PatientService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/medical-reports")
public class MedicalReportController {

    private final MedicalReportService reportService;
    private final PatientService patientService;

    public MedicalReportController(MedicalReportService reportService,
                                   PatientService patientService) {
        this.reportService = reportService;
        this.patientService = patientService;
    }

    // Upload a medical report for a patient
    @PostMapping("/upload/{patientId}")
    public MedicalReportResponse uploadReport(@PathVariable Long patientId,
                                              @RequestParam("file") MultipartFile file) {
        // Verify patient exists
        patientService.getById(patientId);

        try {
            // Create patient-specific folder if it doesn't exist
            File uploadDir = new File("uploads/patient_" + patientId);
            if (!uploadDir.exists()) uploadDir.mkdirs();

            // Save file locally
            File dest = new File(uploadDir, file.getOriginalFilename());
            file.transferTo(dest);

            // Save MedicalReport entity
            MedicalReport report = new MedicalReport();
            report.setPatientId(patientId);
            report.setFileName(file.getOriginalFilename());
            report.setFilePath(dest.getAbsolutePath());

            MedicalReport savedReport = reportService.save(report);

            // Return DTO
            return reportService.toResponse(savedReport);

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload report: " + e.getMessage());
        }
    }

    // Get all reports
    @GetMapping
    public List<MedicalReportResponse> getAllReports() {
        return reportService.toResponseList(reportService.getAll());
    }

    // Get reports by patient
    @GetMapping("/patient/{patientId}")
    public List<MedicalReportResponse> getReportsByPatient(@PathVariable Long patientId) {
        // Verify patient exists
        patientService.getById(patientId);

        return reportService.toResponseList(reportService.getByPatientId(patientId));
    }

    // Get report by ID
    @GetMapping("/{id}")
    public MedicalReportResponse getReportById(@PathVariable Long id) {
        return reportService.toResponse(reportService.getById(id));
    }
}