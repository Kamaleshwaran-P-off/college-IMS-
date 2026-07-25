package com.smartcampus.platform.notification.broadcast.dto;

import java.time.LocalDateTime;

public class NotificationResponse {
  private Long id;
  private String title;
  private String message;
  private String senderRole;
  private String targetRole;
  private String department;
  private String className;
  private LocalDateTime createdAt;
  private Long createdById;
  private String createdByName;
  private boolean read;

  public NotificationResponse(
      Long id,
      String title,
      String message,
      String senderRole,
      String targetRole,
      String department,
      String className,
      LocalDateTime createdAt,
      Long createdById,
      String createdByName,
      boolean read
  ) {
    this.id = id;
    this.title = title;
    this.message = message;
    this.senderRole = senderRole;
    this.targetRole = targetRole;
    this.department = department;
    this.className = className;
    this.createdAt = createdAt;
    this.createdById = createdById;
    this.createdByName = createdByName;
    this.read = read;
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

  public String getSenderRole() {
    return senderRole;
  }

  public String getTargetRole() {
    return targetRole;
  }

  public String getDepartment() {
    return department;
  }

  public String getClassName() {
    return className;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public Long getCreatedById() {
    return createdById;
  }

  public String getCreatedByName() {
    return createdByName;
  }

  public boolean isRead() {
    return read;
  }
}
