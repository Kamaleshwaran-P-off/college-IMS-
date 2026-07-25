package com.smartcampus.platform.ai.dto;

public class AiTextResponse {
  private String result;

  public AiTextResponse() {}

  public AiTextResponse(String result) {
    this.result = result;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }
}
