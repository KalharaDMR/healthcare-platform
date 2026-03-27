package com.healthcare.patient_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.healthcare.patient_service.entity.Patient;

public interface PatientRepository extends JpaRepository<Patient, Long> {
}