package com.healthcare.patient_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.healthcare.patient_service.entity.MedicalReport;

import java.util.List;

public interface MedicalReportRepository extends JpaRepository<MedicalReport, Long> {

    // ✅ Get reports by userId (DB level)
    List<MedicalReport> findByUserId(Long userId);
}