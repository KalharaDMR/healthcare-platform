package com.healthcare.doctor.service;

import com.healthcare.doctor.client.AuthServiceClient;
import com.healthcare.doctor.client.PatientClinicalClient;
import com.healthcare.doctor.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DoctorClinicalService {
    private final PatientClinicalClient patientClinicalClient;
    private final AuthServiceClient authServiceClient;

    @Value("${internal.api.key}")
    private String internalApiKey;

    public DoctorClinicalService(PatientClinicalClient patientClinicalClient, AuthServiceClient authServiceClient) {
        this.patientClinicalClient = patientClinicalClient;
        this.authServiceClient = authServiceClient;
    }

    public PrescriptionDto issuePrescription(String doctorUsername, IssuePrescriptionRequest request) {
        Long patientUserId = resolvePatientUserId(request.getPatientUserId(), request.getPatientUsername());
        CreatePrescriptionPayload payload = new CreatePrescriptionPayload();
        payload.setUserId(patientUserId);
        payload.setDoctorId(doctorUsername);
        payload.setMedications(request.getMedications());
        payload.setNotes(request.getNotes());
        return patientClinicalClient.createPrescription(payload, internalApiKey);
    }

    public List<PrescriptionDto> getPatientPrescriptions(Long patientUserId) {
        return patientClinicalClient.getPrescriptionsByUserForDoctor(patientUserId, internalApiKey);
    }

    public List<MedicalReportDto> getPatientReports(Long patientUserId) {
        return patientClinicalClient.getReportsByUserForDoctor(patientUserId, internalApiKey);
    }

    public PatientMedicalHistoryResponse getPatientMedicalHistory(Long patientUserId) {
        PatientMedicalHistoryResponse response = new PatientMedicalHistoryResponse();
        response.setPatientUserId(patientUserId);
        response.setReports(getPatientReports(patientUserId));
        response.setPrescriptions(getPatientPrescriptions(patientUserId));
        return response;
    }

    public PatientMedicalHistoryResponse getPatientMedicalHistoryByUsername(String patientUsername) {
        Long patientUserId = resolvePatientUserId(null, patientUsername);
        return getPatientMedicalHistory(patientUserId);
    }

    public List<PrescriptionDto> getPatientPrescriptionsByUsername(String patientUsername) {
        Long patientUserId = resolvePatientUserId(null, patientUsername);
        return getPatientPrescriptions(patientUserId);
    }

    public List<MedicalReportDto> getPatientReportsByUsername(String patientUsername) {
        Long patientUserId = resolvePatientUserId(null, patientUsername);
        return getPatientReports(patientUserId);
    }

    private Long resolvePatientUserId(Long patientUserId, String patientUsername) {
        if (patientUserId != null) return patientUserId;
        if (patientUsername == null || patientUsername.isBlank()) {
            throw new RuntimeException("patientUserId or patientUsername is required");
        }
        AuthUserDto user = authServiceClient.getUserByUsername(patientUsername);
        if (user == null || user.getId() == null) {
            throw new RuntimeException("Patient not found");
        }
        return user.getId();
    }
}
