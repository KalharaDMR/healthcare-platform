package com.healthcare.notification_service.controller;

import com.healthcare.notification_service.dto.NotificationRequest;
import com.healthcare.notification_service.model.NotificationLog;
import com.healthcare.notification_service.repository.NotificationLogRepository;
import com.healthcare.notification_service.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailService emailService;
    private final NotificationLogRepository repo;

    @Value("${internal.api.key}")
    private String internalApiKey;

    @PostMapping("/send")
    public ResponseEntity<String> send(
            @RequestHeader(value = "X-INTERNAL-KEY", required = false) String key,
            @Valid @RequestBody NotificationRequest req) {
        if (internalApiKey == null || key == null || !internalApiKey.equals(key)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
        }
        try {
            emailService.sendEmail(
                    req.getRecipient().trim(),
                    req.getSubject().trim(),
                    req.getMessage().trim()
            );

            repo.save(buildLog(req, "SENT", null));
            return ResponseEntity.ok("Notification sent");

        } catch (Exception e) {
            repo.save(buildLog(req, "FAILED", e.getMessage()));
            return ResponseEntity.internalServerError().body("Failed to send notification");
        }
    }

    private NotificationLog buildLog(NotificationRequest req, String status, String errorMessage) {
        NotificationLog log = new NotificationLog();
        log.setRecipient(req.getRecipient());
        log.setSubject(req.getSubject());
        log.setMessage(req.getMessage());
        log.setType(req.getType());
        log.setStatus(status);
        log.setErrorMessage(errorMessage);
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }
}