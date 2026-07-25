package com.smartcampus.platform.notification.broadcast.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartcampus.platform.notification.broadcast.entity.BroadcastNotification;
import com.smartcampus.platform.notification.broadcast.entity.NotificationTargetRole;

public interface BroadcastNotificationRepository extends JpaRepository<BroadcastNotification, Long> {
  @Query("""
      select n from BroadcastNotification n
      where n.targetRole in :targets
        and (:department is null or n.department is null or lower(n.department) = lower(:department))
        and (:className is null or n.className is null or lower(n.className) = lower(:className))
      order by n.createdAt desc
      """)
  List<BroadcastNotification> findForUser(
      @Param("targets") List<NotificationTargetRole> targets,
      @Param("department") String department,
      @Param("className") String className
  );
}
