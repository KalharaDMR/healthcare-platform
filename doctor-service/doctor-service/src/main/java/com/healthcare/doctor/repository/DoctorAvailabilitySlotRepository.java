package com.healthcare.doctor.repository;

import com.healthcare.doctor.entity.DoctorAvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface DoctorAvailabilitySlotRepository extends JpaRepository<DoctorAvailabilitySlot, Long> {

    List<DoctorAvailabilitySlot> findByDoctorIdOrderByStartTimeAsc(Long doctorId);

    List<DoctorAvailabilitySlot> findByDoctorIdAndAvailableTrueAndStartTimeGreaterThanEqualAndEndTimeLessThanEqualOrderByStartTimeAsc(
            Long doctorId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}