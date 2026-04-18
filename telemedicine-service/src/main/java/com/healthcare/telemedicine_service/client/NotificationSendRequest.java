package com.healthcare.telemedicine_service.client;

import lombok.Data;

@Data
public class NotificationSendRequest {
    private String recipient;
    private String subject;
    private String message;
    private String type;
}