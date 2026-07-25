package com.smartcampus.platform.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
  List<Notification> findTop20ByUserIdOrderByCreatedAtDesc(Long userId);
  long countByUserIdAndReadFalse(Long userId);
}
