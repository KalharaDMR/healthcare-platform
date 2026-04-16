package com.example.paymentService.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long slotId;

    @Column(nullable = false)
    private BigDecimal amount;

    private String stripePaymentIntentId;

    private String stripeRefundId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    private LocalDateTime createdAt;

    private String patientUserName;
}
