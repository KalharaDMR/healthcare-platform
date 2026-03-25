package com.healthcare.patient_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.healthcare.patient_service.entity.Prescription;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
}