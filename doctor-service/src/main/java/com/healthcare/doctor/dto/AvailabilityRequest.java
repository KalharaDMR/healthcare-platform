package com.healthcare.doctor.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class AvailabilityRequest {

    @NotNull(message = "date is required")
    @FutureOrPresent(message = "date must be today or a future date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;

    @NotNull(message = "startTime is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime startTime;

    @NotNull(message = "endTime is required")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime endTime;

    @NotBlank(message = "hospital is required")
    private String hospital;

    private Boolean available;

    private BigDecimal costForTheVideoConferencingAppointment;

    private BigDecimal costForTheNormalAppointment;
    public AvailabilityRequest() {
    }

    public void setCostForTheNormalAppointment(BigDecimal costForTheNormalAppointment)
    {this.costForTheNormalAppointment = costForTheNormalAppointment;}

    public BigDecimal getCostForTheNormalAppointment()
    {
        return costForTheNormalAppointment;
    }

    public void setCostForTheVideoConferencingAppointment(BigDecimal costForTheVideoConferencingAppointment)
    {this.costForTheVideoConferencingAppointment = costForTheVideoConferencingAppointment;}

    public BigDecimal getCostForTheVideoConferencingAppointment(){return costForTheVideoConferencingAppointment;}

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

    public String getHospital() {
        return hospital;
    }

    public void setHospital(String hospital) {
        this.hospital = hospital;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }
}