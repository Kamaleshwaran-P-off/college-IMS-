package com.smartcampus.platform.quiz.dto;

import java.util.List;

public class QuizQuestionsResponse {
  private List<QuizQuestionResponse> questions;
  private int timeLimitSeconds;

  public QuizQuestionsResponse(List<QuizQuestionResponse> questions, int timeLimitSeconds) {
    this.questions = questions;
    this.timeLimitSeconds = timeLimitSeconds;
  }

  public List<QuizQuestionResponse> getQuestions() {
    return questions;
  }

  public int getTimeLimitSeconds() {
    return timeLimitSeconds;
  }
}
