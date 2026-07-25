package com.smartcampus.platform.gmail.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "gmail_emails",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "message_id"})
)
public class Email {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "message_id", nullable = false, length = 128)
  private String messageId;

  @Column(nullable = false)
  private String subject;

  @Column(nullable = false)
  private String sender;

  @Lob
  @Column(nullable = false)
  private String body;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  public Email() {}

  public Email(Long userId, String messageId, String subject, String sender, String body, LocalDateTime createdAt) {
    this.userId = userId;
    this.messageId = messageId;
    this.subject = subject;
    this.sender = sender;
    this.body = body;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getSender() {
    return sender;
  }

  public void setSender(String sender) {
    this.sender = sender;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
