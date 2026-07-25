package com.smartcampus.platform.emailintelligence.dto;

public class EmailInsightResponse {
  private Long id;
  private Long emailId;
  private String messageId;
  private String subject;
  private String sender;
  private String summary;
  private String category;
  private String deadline;
  private String priority;
  private boolean actionRequired;
  private String createdAt;

  public EmailInsightResponse() {}

  public EmailInsightResponse(
      Long id,
      Long emailId,
      String messageId,
      String subject,
      String sender,
      String summary,
      String category,
      String deadline,
      String priority,
      boolean actionRequired,
      String createdAt
  ) {
    this.id = id;
    this.emailId = emailId;
    this.messageId = messageId;
    this.subject = subject;
    this.sender = sender;
    this.summary = summary;
    this.category = category;
    this.deadline = deadline;
    this.priority = priority;
    this.actionRequired = actionRequired;
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

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getDeadline() {
    return deadline;
  }

  public void setDeadline(String deadline) {
    this.deadline = deadline;
  }

  public String getPriority() {
    return priority;
  }

  public void setPriority(String priority) {
    this.priority = priority;
  }

  public boolean isActionRequired() {
    return actionRequired;
  }

  public void setActionRequired(boolean actionRequired) {
    this.actionRequired = actionRequired;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }
}
