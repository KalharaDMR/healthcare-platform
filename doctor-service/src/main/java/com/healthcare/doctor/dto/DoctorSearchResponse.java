package com.healthcare.doctor.dto;

import java.time.LocalDate;
import java.util.List;

public class DoctorSearchResponse {

    private Long userId;
    private String username;
    private String doctorName;
    private String specialization;
    private String location;
    private String verificationStatus;
    private boolean available;
    private List<String> hospitals;
    private List<LocalDate> availableDates;

    public DoctorSearchResponse() {
    }

    public DoctorSearchResponse(Long userId,
                                String username,
                                String doctorName,
                                String specialization,
                                String location,
                                String verificationStatus,
                                boolean available,
                                List<String> hospitals,
                                List<LocalDate> availableDates) {
        this.userId = userId;
        this.username = username;
        this.doctorName = doctorName;
        this.specialization = specialization;
        this.location = location;
        this.verificationStatus = verificationStatus;
        this.available = available;
        this.hospitals = hospitals;
        this.availableDates = availableDates;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public List<String> getHospitals() {
        return hospitals;
    }

    public void setHospitals(List<String> hospitals) {
        this.hospitals = hospitals;
    }

    public List<LocalDate> getAvailableDates() {
        return availableDates;
    }

    public void setAvailableDates(List<LocalDate> availableDates) {
        this.availableDates = availableDates;
    }
}