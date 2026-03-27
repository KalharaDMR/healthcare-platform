package com.healthcare.patient_service.service;

import com.healthcare.patient_service.dto.CreatePatientRequest;
import com.healthcare.patient_service.dto.PatientResponse;
import com.healthcare.patient_service.entity.Patient;
import com.healthcare.patient_service.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PatientService {

    private final PatientRepository repository;

    public PatientService(PatientRepository repository) {
        this.repository = repository;
    }

    public Patient save(Patient patient) {
        if (patient.getUserId() == null || patient.getUserId().isEmpty()) {
            patient.setUserId("PAT-" + UUID.randomUUID().toString().substring(0, 8));
        }
        return repository.save(patient);
    }

    public List<Patient> getAll() {
        return repository.findAll();
    }

    public Patient getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Patient not found"));
    }

    // Convert entity → DTO
    public PatientResponse toResponse(Patient patient) {
        PatientResponse dto = new PatientResponse();
        dto.setId(patient.getId());
        dto.setUserId(patient.getUserId());
        dto.setFirstName(patient.getFirstName());
        dto.setLastName(patient.getLastName());
        dto.setEmail(patient.getEmail());
        dto.setPhone(patient.getPhone());
        dto.setDateOfBirth(patient.getDateOfBirth());
        return dto;
    }

    // Convert list of entities → list of DTOs
    public List<PatientResponse> toResponseList(List<Patient> patients) {
        return patients.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}