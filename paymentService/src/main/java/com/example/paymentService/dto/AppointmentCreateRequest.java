package com.example.paymentService.dto;

import lombok.Data;

@Data
public class AppointmentCreateRequest {
    private Long slotId;

    private String notes;
}
