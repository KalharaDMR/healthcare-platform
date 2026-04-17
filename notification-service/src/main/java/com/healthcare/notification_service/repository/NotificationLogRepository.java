package com.healthcare.notification_service.repository;

import com.healthcare.notification_service.model.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
}