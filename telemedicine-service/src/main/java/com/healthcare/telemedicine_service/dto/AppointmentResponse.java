package com.healthcare.telemedicine_service.dto;
import java.time.LocalDateTime;

public class AppointmentResponse {
    private Long id;
    private String patientUsername;
    private String doctorUsername;
    private String status; // CONFIRMED, etc.
    // other fields as needed
    // getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatientUsername() {
        return patientUsername;
    }

    public void setPatientUsername(String patientUsername) {
        this.patientUsername = patientUsername;
    }

    public String getDoctorUsername() {
        return doctorUsername;
    }

    public void setDoctorUsername(String doctorUsername) {
        this.doctorUsername = doctorUsername;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }


}