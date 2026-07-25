package com.smartcampus.platform.ai.dto;

public class AiExplainRequest {
  private String topic;
  private String context;

  public AiExplainRequest() {}

  public AiExplainRequest(String topic, String context) {
    this.topic = topic;
    this.context = context;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }
}
