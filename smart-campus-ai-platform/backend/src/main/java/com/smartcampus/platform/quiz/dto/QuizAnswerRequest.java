package com.smartcampus.platform.quiz.dto;

import com.smartcampus.platform.quiz.entity.QuizOption;
import jakarta.validation.constraints.NotNull;

public class QuizAnswerRequest {
  @NotNull
  private Long questionId;

  @NotNull
  private QuizOption selectedOption;

  public Long getQuestionId() {
    return questionId;
  }

  public void setQuestionId(Long questionId) {
    this.questionId = questionId;
  }

  public QuizOption getSelectedOption() {
    return selectedOption;
  }

  public void setSelectedOption(QuizOption selectedOption) {
    this.selectedOption = selectedOption;
  }
}
