package com.healthcare.appointment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "notification-service")
public interface NotificationServiceClient {

    @PostMapping("/api/notifications/send")
    void send(@RequestBody NotificationSendRequest body,
              @RequestHeader("X-INTERNAL-KEY") String internalKey);
}