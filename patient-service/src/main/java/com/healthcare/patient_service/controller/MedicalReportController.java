package com.healthcare.patient_service.controller;

import com.healthcare.patient_service.client.AuthClient;
import com.healthcare.patient_service.dto.MedicalReportResponse;
import com.healthcare.patient_service.entity.MedicalReport;
import com.healthcare.patient_service.service.MedicalReportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("/api/medical-reports")
public class MedicalReportController {

    private final MedicalReportService reportService;
    private final AuthClient authClient;

    @Value("${internal.api.key}")
    private String apiKey;

    public MedicalReportController(MedicalReportService reportService,
                                   AuthClient authClient) {
        this.reportService = reportService;
        this.authClient = authClient;
    }

    @PostMapping("/upload/{userId}")
    public MedicalReportResponse upload(@PathVariable Long userId,
                                        @RequestParam("file") MultipartFile file) {

        authClient.getUserById(userId, apiKey);

        try {
            File dir = new File("uploads/patient_" + userId);
            if (!dir.exists()) dir.mkdirs();

            File dest = new File(dir, file.getOriginalFilename());
            file.transferTo(dest);

            MedicalReport report = new MedicalReport();
            report.setUserId(userId);
            report.setFileName(file.getOriginalFilename());
            report.setFilePath(dest.getAbsolutePath());

            return reportService.toResponse(reportService.save(report));

        } catch (Exception e) {
            throw new RuntimeException("Upload failed");
        }
    }

    @GetMapping("/user/{userId}")
    public List<MedicalReportResponse> getByUser(@PathVariable Long userId) {

        authClient.getUserById(userId, apiKey);

        return reportService.toResponseList(
                reportService.getByUserId(userId)
        );
    }
}