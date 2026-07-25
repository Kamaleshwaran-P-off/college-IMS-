package com.smartcampus.platform.gmail.dto;

public class StoredEmailResponse {
  private Long id;
  private String messageId;
  private String subject;
  private String sender;
  private String body;
  private String createdAt;

  public StoredEmailResponse() {}

  public StoredEmailResponse(Long id, String messageId, String subject, String sender, String body, String createdAt) {
    this.id = id;
    this.messageId = messageId;
    this.subject = subject;
    this.sender = sender;
    this.body = body;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public String getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(String createdAt) {
    this.createdAt = createdAt;
  }
}
