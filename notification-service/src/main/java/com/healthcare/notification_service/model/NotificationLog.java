package com.healthcare.notification_service.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String recipient;
    private String subject;

    @Column(length = 3000)
    private String message;

    private String type;   // PAYMENT / APPOINTMENT / EMAIL
    private String status; // SENT / FAILED

    @Column(length = 2000)
    private String errorMessage;

    private LocalDateTime createdAt;
}