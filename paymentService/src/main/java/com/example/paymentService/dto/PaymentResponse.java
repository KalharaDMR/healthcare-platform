package com.example.paymentService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PaymentResponse {
    private boolean success;
    private String clientSecret;
}
