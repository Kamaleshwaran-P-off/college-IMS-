package com.smartcampus.platform.emailintelligence.hackathon.dto;

public class EmailHackathonResponse {
  private Long id;
  private Long emailId;
  private String messageId;
  private String name;
  private String subject;
  private String sender;
  private String deadline;
  private String createdAt;

  public EmailHackathonResponse() {}

  public EmailHackathonResponse(
      Long id,
      Long emailId,
      String messageId,
      String name,
      String subject,
      String sender,
      String deadline,
      String createdAt
  ) {
    this.id = id;
    this.emailId = emailId;
    this.messageId = messageId;
    this.name = name;
    this.subject = subject;
    this.sender = sender;
    this.deadline = deadline;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Long getEmailId() {
    return emailId;
  }

  public void setEmailId(Long emailId) {
    this.emailId = emailId;
  }

  public String getMessageId() {
    return messageId;
  }

  public void setMessageId(String messageId) {
    this.messageId = messageId;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public String getDeadline() {
    return deadline;
  }

  public void setDeadline(String deadline) {
    this.deadline = deadline;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }
}
