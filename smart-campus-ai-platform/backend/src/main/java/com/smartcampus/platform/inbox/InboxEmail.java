package com.smartcampus.platform.inbox;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "inbox_emails")
public class InboxEmail {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String subject;

  @Column(nullable = false)
  private String sender;

  @Lob
  private String content;

  @Column(nullable = false)
  private LocalDateTime receivedAt;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EmailCategory category;

  @Column(nullable = false)
  private boolean important;

  public InboxEmail() {}

  public InboxEmail(String subject, String sender, String content, LocalDateTime receivedAt, EmailCategory category, boolean important) {
    this.subject = subject;
    this.sender = sender;
    this.content = content;
    this.receivedAt = receivedAt;
    this.category = category;
    this.important = important;
  }

  public Long getId() {
    return id;
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

  public boolean isImportant() {
    return important;
  }

  public void setImportant(boolean important) {
    this.important = important;
  }
}
