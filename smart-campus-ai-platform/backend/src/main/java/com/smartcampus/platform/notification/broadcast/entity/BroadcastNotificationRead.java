package com.smartcampus.platform.notification.broadcast.entity;

import java.time.LocalDateTime;

import com.smartcampus.platform.auth.entity.User;
import jakarta.persistence.*;

@Entity
@Table(
    name = "broadcast_notification_reads",
    uniqueConstraints = @UniqueConstraint(columnNames = {"notification_id", "user_id"})
)
public class BroadcastNotificationRead {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "notification_id", nullable = false)
  private BroadcastNotification notification;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false, updatable = false)
  private LocalDateTime readAt;

  public BroadcastNotificationRead() {}

  public BroadcastNotificationRead(BroadcastNotification notification, User user) {
    this.notification = notification;
    this.user = user;
  }

  @PrePersist
  public void onCreate() {
    if (readAt == null) {
      readAt = LocalDateTime.now();
    }
  }

  public Long getId() {
    return id;
  }

  public BroadcastNotification getNotification() {
    return notification;
  }

  public User getUser() {
    return user;
  }

  public LocalDateTime getReadAt() {
    return readAt;
  }
}
