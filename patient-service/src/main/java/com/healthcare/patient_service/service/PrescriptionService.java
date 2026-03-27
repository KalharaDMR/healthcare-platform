package com.healthcare.patient_service.service;

import com.healthcare.patient_service.dto.PrescriptionResponse;
import com.healthcare.patient_service.entity.Prescription;
import com.healthcare.patient_service.repository.PrescriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {

    private final PrescriptionRepository repository;

    public PrescriptionService(PrescriptionRepository repository) {
        this.repository = repository;
    }

    // Save a prescription
    public Prescription save(Prescription prescription) {
        return repository.save(prescription);
    }

    // Get all prescriptions
    public List<Prescription> getAll() {
        return repository.findAll();
    }

    // Get prescription by ID
    public Prescription getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found with ID: " + id));
    }

    // Get all prescriptions for a patient
    public List<Prescription> getByPatientId(Long patientId) {
        return repository.findAll().stream()
                .filter(p -> p.getPatientId().equals(patientId))
                .collect(Collectors.toList());
    }

    // Convert Prescription entity → PrescriptionResponse DTO
    public PrescriptionResponse toResponse(Prescription prescription) {
        PrescriptionResponse dto = new PrescriptionResponse();
        dto.setId(prescription.getId());
        dto.setPatientId(prescription.getPatientId());
        dto.setDoctorId(prescription.getDoctorId());
        dto.setMedications(prescription.getMedications());
        dto.setNotes(prescription.getNotes());
        return dto;
    }

    // Convert list of Prescription entities → list of DTOs
    public List<PrescriptionResponse> toResponseList(List<Prescription> prescriptions) {
        return prescriptions.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}