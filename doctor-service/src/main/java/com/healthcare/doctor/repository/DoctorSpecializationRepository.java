package com.healthcare.doctor.repository;

import com.healthcare.doctor.entity.DoctorSpecialization;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DoctorSpecializationRepository extends JpaRepository<DoctorSpecialization, Long> {

    Optional<DoctorSpecialization> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<DoctorSpecialization> findByActiveTrueOrderByNameAsc();

    List<DoctorSpecialization> findAllByOrderByNameAsc();
}