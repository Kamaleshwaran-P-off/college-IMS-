package com.smartcampus.platform.learning.dto;

import java.util.List;

public class QuizSubmitRequest {
  private List<String> answers;

  public QuizSubmitRequest() {}

  public QuizSubmitRequest(List<String> answers) {
    this.answers = answers;
  }

  public List<String> getAnswers() {
    return answers;
  }

  public void setAnswers(List<String> answers) {
    this.answers = answers;
  }
}
