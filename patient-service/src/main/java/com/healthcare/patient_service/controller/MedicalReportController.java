package com.healthcare.patient_service.controller;

import com.healthcare.patient_service.client.AuthClient;
import com.healthcare.patient_service.dto.MedicalReportResponse;
import com.healthcare.patient_service.entity.MedicalReport;
import com.healthcare.patient_service.service.MedicalReportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/medical-reports")
public class MedicalReportController {

    private final MedicalReportService reportService;
    private final AuthClient authClient;

    @Value("${internal.api.key}")
    private String apiKey;

    @Value("${spring.file.upload-dir:uploads}")
    private String uploadDir;

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
            if (file.isEmpty()) {
                throw new RuntimeException("Uploaded file is empty");
            }

            String originalName = file.getOriginalFilename() == null ? "report" : file.getOriginalFilename();
            String safeName = Paths.get(originalName).getFileName().toString();
            String storedName = UUID.randomUUID() + "_" + safeName;

            Path patientDir = Paths.get(uploadDir, "patient_" + userId).toAbsolutePath().normalize();
            Files.createDirectories(patientDir);

            Path dest = patientDir.resolve(storedName).normalize();
            file.transferTo(dest);

            MedicalReport report = new MedicalReport();
            report.setUserId(userId);
            report.setFileName(safeName);
            report.setFilePath(dest.toString());

            return reportService.toResponse(reportService.save(report));

        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }

    @PostMapping("/upload/me")
    public MedicalReportResponse uploadMyReport(@RequestHeader(value = "X-User-Id", required = false) String username,
                                                @RequestParam("file") MultipartFile file) {
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Missing user header");
        }
        Long userId = authClient.getUserByUsername(username).getId();
        return upload(userId, file);
    }

    @GetMapping("/user/{userId}")
    public List<MedicalReportResponse> getByUser(@PathVariable Long userId) {

        authClient.getUserById(userId, apiKey);

        return reportService.toResponseList(
                reportService.getByUserId(userId)
        );
    }

    @GetMapping("/me")
    public List<MedicalReportResponse> getMyReports(
            @RequestHeader(value = "X-User-Id", required = false) String username) {
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Missing user header");
        }
        Long userId = authClient.getUserByUsername(username).getId();
        return getByUser(userId);
    }

    @GetMapping("/doctor/user/{userId}")
    public List<MedicalReportResponse> getByUserForDoctor(@PathVariable Long userId,
                                                          @RequestHeader(value = "X-User-Role", required = false) String rolesHeader,
                                                          @RequestHeader(value = "X-INTERNAL-KEY", required = false) String internalKey) {
        ensureDoctorOrInternalAccess(rolesHeader, internalKey);
        authClient.getUserById(userId, apiKey);
        return reportService.toResponseList(reportService.getByUserId(userId));
    }

    @GetMapping("/{reportId}/content/me")
    public ResponseEntity<Resource> getMyReportContent(@PathVariable Long reportId,
                                                       @RequestHeader(value = "X-User-Id", required = false) String username) {
        if (username == null || username.isBlank()) {
            throw new RuntimeException("Missing user header");
        }

        Long userId = authClient.getUserByUsername(username).getId();
        MedicalReport report = reportService.getById(reportId);
        if (!userId.equals(report.getUserId())) {
            throw new RuntimeException("Access denied");
        }

        return buildFileResponse(report);
    }

    @GetMapping("/{reportId}/content")
    public ResponseEntity<Resource> getReportContentForDoctor(@PathVariable Long reportId,
                                                              @RequestHeader(value = "X-User-Role", required = false) String rolesHeader,
                                                              @RequestHeader(value = "X-INTERNAL-KEY", required = false) String internalKey) {
        ensureDoctorOrInternalAccess(rolesHeader, internalKey);
        MedicalReport report = reportService.getById(reportId);
        return buildFileResponse(report);
    }

    private void ensureDoctorOrInternalAccess(String rolesHeader, String internalKey) {
        if (apiKey.equals(internalKey)) return;
        if (rolesHeader == null || rolesHeader.isBlank()) {
            throw new RuntimeException("Doctor access required");
        }

        boolean isDoctor = List.of(rolesHeader.split(",")).stream()
                .map(String::trim)
                .map(r -> r.startsWith("ROLE_") ? r.substring(5) : r)
                .map(r -> r.toUpperCase(Locale.ROOT))
                .anyMatch("DOCTOR"::equals);

        if (!isDoctor) {
            throw new RuntimeException("Doctor access required");
        }
    }

    private ResponseEntity<Resource> buildFileResponse(MedicalReport report) {
        try {
            Path filePath = Paths.get(report.getFilePath()).toAbsolutePath().normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw new RuntimeException("Report file not found");
            }

            String contentType = Files.probeContentType(filePath);
            if (contentType == null || contentType.isBlank()) {
                contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition.inline().filename(report.getFileName()).build().toString())
                    .body(resource);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException("Failed to read report file: " + ex.getMessage());
        }
    }
}