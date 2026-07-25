package com.smartcampus.platform.quiz.dto;

import java.util.List;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class QuizSubmissionRequest {
  @NotNull
  private Long userId;

  @NotNull
  @Size(min = 2, max = 2)
  private List<QuizAnswerRequest> answers;

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public List<QuizAnswerRequest> getAnswers() {
    return answers;
  }

  public void setAnswers(List<QuizAnswerRequest> answers) {
    this.answers = answers;
  }
}
