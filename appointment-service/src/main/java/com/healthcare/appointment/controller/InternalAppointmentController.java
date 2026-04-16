package com.healthcare.appointment.controller;

import com.healthcare.appointment.dto.AppointmentResponse;
import com.healthcare.appointment.entity.Appointment;
import com.healthcare.appointment.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/appointments")
public class InternalAppointmentController {

    private final AppointmentRepository appointmentRepository;

    @Value("${internal.api.key}")
    private String internalApiKey;

    public InternalAppointmentController(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    private void validateApiKey(String apiKey) {
        if (apiKey == null || !apiKey.equals(internalApiKey)) {
            throw new RuntimeException("Unauthorized");
        }
    }

    @GetMapping("/{id}")
    public AppointmentResponse getAppointmentById(@PathVariable Long id,
                                                  @RequestHeader(value = "X-INTERNAL-KEY", required = false) String apiKey) {
        validateApiKey(apiKey);
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));
        return toResponse(appointment);
    }

    private AppointmentResponse toResponse(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setPatientUsername(appointment.getPatientUsername());
        response.setDoctorUsername(appointment.getDoctorUsername());
        response.setStatus(appointment.getStatus());
        response.setAppointmentDate(appointment.getAppointmentDate());
        response.setStartTime(appointment.getStartTime());
        response.setEndTime(appointment.getEndTime());
        response.setHospital(appointment.getHospital());
        response.setNotes(appointment.getNotes());
        response.setCreatedAt(appointment.getCreatedAt());
        response.setUpdatedAt(appointment.getUpdatedAt());
        return response;
    }
}