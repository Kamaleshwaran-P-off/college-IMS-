package com.smartcampus.platform.inbox.dto;

import java.time.LocalDateTime;

import com.smartcampus.platform.inbox.EmailCategory;

public class InboxEmailResponse {
  private Long id;
  private String subject;
  private String sender;
  private String content;
  private LocalDateTime receivedAt;
  private EmailCategory category;
  private EmailCategory suggestedCategory;
  private boolean important;

  public InboxEmailResponse() {}

  public InboxEmailResponse(
      Long id,
      String subject,
      String sender,
      String content,
      LocalDateTime receivedAt,
      EmailCategory category,
      EmailCategory suggestedCategory,
      boolean important
  ) {
    this.id = id;
    this.subject = subject;
    this.sender = sender;
    this.content = content;
    this.receivedAt = receivedAt;
    this.category = category;
    this.suggestedCategory = suggestedCategory;
    this.important = important;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public LocalDateTime getReceivedAt() {
    return receivedAt;
  }

  public void setReceivedAt(LocalDateTime receivedAt) {
    this.receivedAt = receivedAt;
  }

  public EmailCategory getCategory() {
    return category;
  }

  public void setCategory(EmailCategory category) {
    this.category = category;
  }

  public EmailCategory getSuggestedCategory() {
    return suggestedCategory;
  }

  public void setSuggestedCategory(EmailCategory suggestedCategory) {
    this.suggestedCategory = suggestedCategory;
  }

  public boolean isImportant() {
    return important;
  }

  public void setImportant(boolean important) {
    this.important = important;
  }
}
