package com.healthcare.doctor.repository;

import com.healthcare.doctor.entity.DoctorMeta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorMetaRepository extends JpaRepository<DoctorMeta, Long> {

    Optional<DoctorMeta> findByUsername(String username);

    List<DoctorMeta> findByVerificationStatusIgnoreCase(String verificationStatus);
}