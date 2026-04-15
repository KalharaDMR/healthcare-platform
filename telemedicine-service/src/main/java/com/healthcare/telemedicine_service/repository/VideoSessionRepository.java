package com.healthcare.telemedicine_service.repository;
import com.healthcare.telemedicine_service.entity.VideoSession;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VideoSessionRepository extends JpaRepository<VideoSession, Long> {
    Optional<VideoSession> findByAppointmentId(Long appointmentId);
}