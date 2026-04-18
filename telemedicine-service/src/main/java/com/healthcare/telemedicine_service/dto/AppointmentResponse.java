//package com.healthcare.telemedicine_service.dto;
//import java.time.LocalDateTime;
//
//public class AppointmentResponse {
//    private Long id;
//    private String patientUsername;
//    private String doctorUsername;
//    private String status; // CONFIRMED, etc.
//    // other fields as needed
//    // getters and setters
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getPatientUsername() {
//        return patientUsername;
//    }
//
//    public void setPatientUsername(String patientUsername) {
//        this.patientUsername = patientUsername;
//    }
//
//    public String getDoctorUsername() {
//        return doctorUsername;
//    }
//
//    public void setDoctorUsername(String doctorUsername) {
//        this.doctorUsername = doctorUsername;
//    }
//
//    public String getStatus() {
//        return status;
//    }
//
//    public void setStatus(String status) {
//        this.status = status;
//    }
//
//
//}




package com.healthcare.telemedicine_service.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class AppointmentResponse {
    private Long id;
    private String patientUsername;
    private String doctorUsername;
    private String status;
    private String hospital;
    private LocalDate appointmentDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String notes;

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

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public LocalDate getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(LocalDate appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}