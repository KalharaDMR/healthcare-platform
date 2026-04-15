package com.healthcare.appointment.dto;

import jakarta.validation.constraints.NotNull;

public class AppointmentRescheduleRequest {

    @NotNull(message = "newSlotId is required")
    private Long newSlotId;

    public AppointmentRescheduleRequest() {
    }

    public Long getNewSlotId() {
        return newSlotId;
    }

    public void setNewSlotId(Long newSlotId) {
        this.newSlotId = newSlotId;
    }
}