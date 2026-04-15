package com.healthcare.patient_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.healthcare.patient_service.entity.Prescription;

import java.util.List;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    // ✅ Get prescriptions by userId (DB level, FAST)
    List<Prescription> findByUserId(Long userId);
}