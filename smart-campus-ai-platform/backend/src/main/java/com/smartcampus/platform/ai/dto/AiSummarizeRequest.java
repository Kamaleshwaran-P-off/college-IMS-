package com.smartcampus.platform.ai.dto;

public class AiSummarizeRequest {
  private String content;

  public AiSummarizeRequest() {}

  public AiSummarizeRequest(String content) {
    this.content = content;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
