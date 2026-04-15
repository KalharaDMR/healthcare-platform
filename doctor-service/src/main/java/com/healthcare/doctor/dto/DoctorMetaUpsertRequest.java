package com.healthcare.doctor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DoctorMetaUpsertRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "doctorName is required")
    private String doctorName;

    @NotBlank(message = "specialization is required")
    private String specialization;

    @NotBlank(message = "location is required")
    private String location;

    @NotBlank(message = "verificationStatus is required")
    private String verificationStatus;

    public DoctorMetaUpsertRequest() {
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
}