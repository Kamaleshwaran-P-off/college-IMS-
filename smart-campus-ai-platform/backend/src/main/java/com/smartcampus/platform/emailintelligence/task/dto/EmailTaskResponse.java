package com.smartcampus.platform.emailintelligence.task.dto;

public class EmailTaskResponse {
  private Long id;
  private Long emailId;
  private String subject;
  private String title;
  private String deadline;
  private String priority;
  private boolean completed;
  private String createdAt;

  public EmailTaskResponse() {}

  public EmailTaskResponse(
      Long id,
      Long emailId,
      String subject,
      String title,
      String deadline,
      String priority,
      boolean completed,
      String createdAt
  ) {
    this.id = id;
    this.emailId = emailId;
    this.subject = subject;
    this.title = title;
    this.deadline = deadline;
    this.priority = priority;
    this.completed = completed;
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

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
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

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }
}
