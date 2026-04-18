package com.healthcare.notification_service.service;

public interface EmailService {
    void sendEmail(String to, String subject, String message);
}