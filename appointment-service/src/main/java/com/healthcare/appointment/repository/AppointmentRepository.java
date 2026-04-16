package com.healthcare.appointment.repository;

import com.healthcare.appointment.entity.Appointment;
import com.healthcare.appointment.entity.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientUsernameOrderByCreatedAtDesc(String patientUsername);

    List<Appointment> findByDoctorUsernameOrderByAppointmentDateAscStartTimeAsc(String doctorUsername);

    List<Appointment> findByPatientUsernameAndStatusOrderByCreatedAtDesc(String patientUsername, AppointmentStatus status);

    List<Appointment> findByDoctorUsernameAndStatusOrderByAppointmentDateAscStartTimeAsc(String doctorUsername, AppointmentStatus status);

    Optional<Appointment> findByIdAndPatientUsername(Long id, String patientUsername);

    Optional<Appointment> findByIdAndDoctorUsername(Long id, String doctorUsername);
}