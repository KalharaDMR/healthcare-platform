package com.healthcare.appointment.client;

import lombok.Data;

@Data
public class NotificationSendRequest {
    private String recipient;
    private String subject;
    private String message;
    private String type;
}