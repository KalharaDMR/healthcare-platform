package com.example.paymentService.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookRequest {
    @NotNull(message = "Slot id is required")
    private Long slotId;

    private String notes;
}
