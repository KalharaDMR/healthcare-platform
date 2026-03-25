package com.healthcare.patient_service.service;

import com.healthcare.patient_service.entity.Patient;
import com.healthcare.patient_service.repository.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientService {

    private final PatientRepository repository;

    public PatientService(PatientRepository repository) {
        this.repository = repository;
    }

    public Patient save(Patient patient) {
        return repository.save(patient);
    }

    public List<Patient> getAll() {
        return repository.findAll();
    }

    public Patient getById(Long id) {
        return repository.findById(id).orElseThrow(() -> new RuntimeException("Patient not found"));
    }
}