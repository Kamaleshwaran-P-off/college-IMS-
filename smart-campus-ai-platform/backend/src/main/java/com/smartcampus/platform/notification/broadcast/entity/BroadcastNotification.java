package com.smartcampus.platform.notification.broadcast.entity;

import java.time.LocalDateTime;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.entity.User;
import jakarta.persistence.*;

@Entity
@Table(name = "broadcast_notifications")
public class BroadcastNotification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Lob
  private String message;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role senderRole;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationTargetRole targetRole;

  @Column(length = 100)
  private String department;

  @Column(length = 50)
  private String className;

  @ManyToOne(optional = false)
  @JoinColumn(name = "created_by", nullable = false)
  private User createdBy;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public BroadcastNotification() {}

  public BroadcastNotification(
      String title,
      String message,
      Role senderRole,
      NotificationTargetRole targetRole,
      String department,
      String className,
      User createdBy
  ) {
    this.title = title;
    this.message = message;
    this.senderRole = senderRole;
    this.targetRole = targetRole;
    this.department = department;
    this.className = className;
    this.createdBy = createdBy;
  }

  @PrePersist
  public void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getMessage() {
    return message;
  }

  public Role getSenderRole() {
    return senderRole;
  }

  public NotificationTargetRole getTargetRole() {
    return targetRole;
  }

  public String getDepartment() {
    return department;
  }

  public String getClassName() {
    return className;
  }

  public User getCreatedBy() {
    return createdBy;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
