package com.smartcampus.platform.notification.dto;

import java.time.LocalDateTime;

import com.smartcampus.platform.notification.entity.NotificationType;

public class NotificationResponse {
  private Long id;
  private NotificationType type;
  private String title;
  private String message;
  private boolean read;
  private LocalDateTime createdAt;

  public NotificationResponse(
      Long id,
      NotificationType type,
      String title,
      String message,
      boolean read,
      LocalDateTime createdAt
  ) {
    this.id = id;
    this.type = type;
    this.title = title;
    this.message = message;
    this.read = read;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public NotificationType getType() {
    return type;
  }

  public String getTitle() {
    return title;
  }

  public String getMessage() {
    return message;
  }

  public boolean isRead() {
    return read;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
