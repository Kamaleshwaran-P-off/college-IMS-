package com.smartcampus.platform.gmail.dto;

public class GmailEmailResponse {
  private String id;
  private String subject;
  private String sender;
  private String content;
  private String date;
  private String category;
  private Boolean important;

  public GmailEmailResponse() {}

  public GmailEmailResponse(String id, String subject, String sender, String content, String date, String category, Boolean important) {
    this.id = id;
    this.subject = subject;
    this.sender = sender;
    this.content = content;
    this.date = date;
    this.category = category;
    this.important = important;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
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

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
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
}
