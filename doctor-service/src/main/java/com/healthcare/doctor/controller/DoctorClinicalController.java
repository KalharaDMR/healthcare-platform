package com.healthcare.doctor.controller;

import com.healthcare.doctor.dto.*;
import com.healthcare.doctor.service.DoctorClinicalService;
import com.healthcare.doctor.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/doctor/clinical")
public class DoctorClinicalController {
    private final DoctorClinicalService doctorClinicalService;
    private final JwtUtil jwtUtil;

    public DoctorClinicalController(DoctorClinicalService doctorClinicalService, JwtUtil jwtUtil) {
        this.doctorClinicalService = doctorClinicalService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/prescriptions")
    public ResponseEntity<PrescriptionDto> issuePrescription(
            @RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader,
            @RequestHeader(value = "X-User-Role", required = false) String rolesHeader,
            @Valid @RequestBody IssuePrescriptionRequest request) {
        ensureDoctorRole(rolesHeader);
        String doctorUsername = extractUsernameFromAuthorizationHeader(authorizationHeader);
        return ResponseEntity.ok(doctorClinicalService.issuePrescription(doctorUsername, request));
    }

    @GetMapping("/patients/{patientUserId}/prescriptions")
    public ResponseEntity<List<PrescriptionDto>> getPatientPrescriptions(
            @RequestHeader(value = "X-User-Role", required = false) String rolesHeader,
            @PathVariable Long patientUserId) {
        ensureDoctorRole(rolesHeader);
        return ResponseEntity.ok(doctorClinicalService.getPatientPrescriptions(patientUserId));
    }

    @GetMapping("/patients/by-username/{patientUsername}/prescriptions")
    public ResponseEntity<List<PrescriptionDto>> getPatientPrescriptionsByUsername(
            @RequestHeader(value = "X-User-Role", required = false) String rolesHeader,
            @PathVariable String patientUsername) {
        ensureDoctorRole(rolesHeader);
        return ResponseEntity.ok(doctorClinicalService.getPatientPrescriptionsByUsername(patientUsername));
    }

    @GetMapping("/patients/{patientUserId}/reports")
    public ResponseEntity<List<MedicalReportDto>> getPatientReports(
            @RequestHeader(value = "X-User-Role", required = false) String rolesHeader,
            @PathVariable Long patientUserId) {
        ensureDoctorRole(rolesHeader);
        return ResponseEntity.ok(doctorClinicalService.getPatientReports(patientUserId));
    }

    @GetMapping("/patients/by-username/{patientUsername}/reports")
    public ResponseEntity<List<MedicalReportDto>> getPatientReportsByUsername(
            @RequestHeader(value = "X-User-Role", required = false) String rolesHeader,
            @PathVariable String patientUsername) {
        ensureDoctorRole(rolesHeader);
        return ResponseEntity.ok(doctorClinicalService.getPatientReportsByUsername(patientUsername));
    }

    @GetMapping("/patients/{patientUserId}/medical-history")
    public ResponseEntity<PatientMedicalHistoryResponse> getPatientMedicalHistory(
            @RequestHeader(value = "X-User-Role", required = false) String rolesHeader,
            @PathVariable Long patientUserId) {
        ensureDoctorRole(rolesHeader);
        return ResponseEntity.ok(doctorClinicalService.getPatientMedicalHistory(patientUserId));
    }

    @GetMapping("/patients/by-username/{patientUsername}/medical-history")
    public ResponseEntity<PatientMedicalHistoryResponse> getPatientMedicalHistoryByUsername(
            @RequestHeader(value = "X-User-Role", required = false) String rolesHeader,
            @PathVariable String patientUsername) {
        ensureDoctorRole(rolesHeader);
        return ResponseEntity.ok(doctorClinicalService.getPatientMedicalHistoryByUsername(patientUsername));
    }

    private String extractUsernameFromAuthorizationHeader(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new RuntimeException("Missing Authorization header");
        }

        if (!authorizationHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Authorization header must start with Bearer");
        }

        String token = authorizationHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        if (username == null || username.isBlank()) {
            throw new RuntimeException("Invalid or expired token");
        }

        return username;
    }

    private void ensureDoctorRole(String rolesHeader) {
        if (rolesHeader == null || rolesHeader.isBlank()) {
            throw new RuntimeException("Doctor role required");
        }

        boolean isDoctor = List.of(rolesHeader.split(",")).stream()
                .map(String::trim)
                .map(role -> role.startsWith("ROLE_") ? role.substring(5) : role)
                .map(role -> role.toUpperCase(Locale.ROOT))
                .anyMatch("DOCTOR"::equals);

        if (!isDoctor) {
            throw new RuntimeException("Doctor role required");
        }
    }
}
