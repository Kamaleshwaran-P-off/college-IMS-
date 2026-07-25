package com.smartcampus.platform.ai.dto;

import java.util.List;

public class AiGenerateQuizRequest {
  private String topic;
  private Integer questionCount;
  private List<String> questionTypes;

  public AiGenerateQuizRequest() {}

  public AiGenerateQuizRequest(String topic, Integer questionCount, List<String> questionTypes) {
    this.topic = topic;
    this.questionCount = questionCount;
    this.questionTypes = questionTypes;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public Integer getQuestionCount() {
    return questionCount;
  }

  public void setQuestionCount(Integer questionCount) {
    this.questionCount = questionCount;
  }

  public List<String> getQuestionTypes() {
    return questionTypes;
  }

  public void setQuestionTypes(List<String> questionTypes) {
    this.questionTypes = questionTypes;
  }
}
