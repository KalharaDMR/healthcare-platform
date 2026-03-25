package com.healthcare.patient_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.healthcare.patient_service.entity.MedicalReport;

public interface MedicalReportRepository extends JpaRepository<MedicalReport, Long> {
}