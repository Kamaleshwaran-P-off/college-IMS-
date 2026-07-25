package com.smartcampus.platform.learning.dto;

public class LearningChatResponse {
  private String reply;

  public LearningChatResponse() {}

  public LearningChatResponse(String reply) {
    this.reply = reply;
  }

  public String getReply() {
    return reply;
  }

  public void setReply(String reply) {
    this.reply = reply;
  }
}
