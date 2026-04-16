package com.healthcare.appointment.dto;

import jakarta.validation.constraints.NotNull;

public class AppointmentCreateRequest {

    @NotNull(message = "slotId is required")
    private Long slotId;

    private String notes;

    public AppointmentCreateRequest() {
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}