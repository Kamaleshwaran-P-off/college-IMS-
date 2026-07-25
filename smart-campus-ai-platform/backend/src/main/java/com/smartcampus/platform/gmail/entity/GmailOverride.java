package com.smartcampus.platform.gmail.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "gmail_overrides", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "message_id"}))
public class GmailOverride {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "message_id", nullable = false)
  private String messageId;

  private String category;

  private Boolean important;

  private LocalDateTime updatedAt;

  public GmailOverride() {}

  public GmailOverride(Long userId, String messageId, String category, Boolean important, LocalDateTime updatedAt) {
    this.userId = userId;
    this.messageId = messageId;
    this.category = category;
    this.important = important;
    this.updatedAt = updatedAt;
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

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public Boolean getImportant() {
    return important;
  }

  public void setImportant(Boolean important) {
    this.important = important;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
