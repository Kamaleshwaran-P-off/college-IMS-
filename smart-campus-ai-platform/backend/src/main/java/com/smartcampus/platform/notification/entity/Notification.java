package com.smartcampus.platform.notification.entity;

import java.time.LocalDateTime;

import com.smartcampus.platform.auth.entity.User;
import jakarta.persistence.*;

@Entity
@Table(name = "notifications")
public class Notification {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private NotificationType type;

  @Column(nullable = false)
  private String title;

  @Lob
  private String message;

  @Column(name = "is_read", nullable = false)
  private boolean read;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public Notification() {}

  public Notification(User user, NotificationType type, String title, String message) {
    this.user = user;
    this.type = type;
    this.title = title;
    this.message = message;
    this.read = false;
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

  public User getUser() {
    return user;
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

  public void setRead(boolean read) {
    this.read = read;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
