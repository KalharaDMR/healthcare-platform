package com.healthcare.doctor.repository;

import com.healthcare.doctor.entity.DoctorProfile;
import com.healthcare.doctor.entity.DoctorVerificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Long> {
    boolean existsByAuthUserId(Long authUserId);
    boolean existsByLicenseNumber(String licenseNumber);
    Optional<DoctorProfile> findByAuthUserId(Long authUserId);
    List<DoctorProfile> findByVerificationStatus(DoctorVerificationStatus verificationStatus);
}