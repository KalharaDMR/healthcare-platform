package com.healthcare.doctor.repository;

import com.healthcare.doctor.entity.AvailabilitySlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AvailabilityRepository extends JpaRepository<AvailabilitySlot, Long> {

    List<AvailabilitySlot> findByDoctorUsernameOrderByDateAscStartTimeAsc(String doctorUsername);

    Optional<AvailabilitySlot> findByIdAndDoctorUsername(Long id, String doctorUsername);
    List<AvailabilitySlot> findByDoctorUsernameAndAvailableTrue(String doctorUsername);

    @Query("""
            SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END
            FROM AvailabilitySlot a
            WHERE a.doctorUsername = :doctorUsername
              AND a.date = :date
              AND (:excludeId IS NULL OR a.id <> :excludeId)
              AND a.startTime < :endTime
              AND a.endTime > :startTime
            """)
    boolean existsOverlappingSlot(
            @Param("doctorUsername") String doctorUsername,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId
    );
}