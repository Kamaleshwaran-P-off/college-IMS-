package com.smartcampus.platform.learning.dto;

public class LearningChatRequest {
  private String message;

  public LearningChatRequest() {}

  public LearningChatRequest(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
