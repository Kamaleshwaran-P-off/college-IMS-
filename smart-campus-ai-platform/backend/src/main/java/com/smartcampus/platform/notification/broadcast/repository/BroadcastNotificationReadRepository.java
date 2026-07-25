package com.smartcampus.platform.notification.broadcast.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.notification.broadcast.entity.BroadcastNotificationRead;

public interface BroadcastNotificationReadRepository extends JpaRepository<BroadcastNotificationRead, Long> {
  List<BroadcastNotificationRead> findByUserIdAndNotificationIdIn(Long userId, List<Long> notificationIds);
  boolean existsByUserIdAndNotificationId(Long userId, Long notificationId);
}
