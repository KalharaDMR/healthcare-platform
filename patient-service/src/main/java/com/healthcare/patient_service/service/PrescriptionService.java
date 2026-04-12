package com.healthcare.patient_service.service;

import com.healthcare.patient_service.client.AuthClient;
import com.healthcare.patient_service.dto.PrescriptionResponse;
import com.healthcare.patient_service.entity.Prescription;
import com.healthcare.patient_service.repository.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {

    private final PrescriptionRepository repository;
    private final AuthClient authClient;

    @Value("${internal.api.key}")
    private String apiKey;

    public PrescriptionService(PrescriptionRepository repository, AuthClient authClient) {
        this.repository = repository;
        this.authClient = authClient;
    }

    public Prescription save(Prescription prescription) {

        if (prescription.getUserId() != null) {
            validateUser(prescription.getUserId());
        }

        return repository.save(prescription);
    }

    public List<Prescription> getAll() {
        return repository.findAll();
    }

    public Prescription getById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));
    }

    public List<Prescription> getByUserId(Long userId) {

        validateUser(userId);

        return repository.findAll().stream()
                .filter(p -> userId.equals(p.getUserId()))
                .collect(Collectors.toList());
    }

    private void validateUser(Long userId) {
        authClient.getUserById(userId, apiKey);
    }

    public PrescriptionResponse toResponse(Prescription p) {
        PrescriptionResponse dto = new PrescriptionResponse();
        dto.setId(p.getId());
        dto.setUserId(p.getUserId());
        dto.setDoctorId(p.getDoctorId());
        dto.setMedications(p.getMedications());
        dto.setNotes(p.getNotes());
        return dto;
    }

    public List<PrescriptionResponse> toResponseList(List<Prescription> list) {
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }
}