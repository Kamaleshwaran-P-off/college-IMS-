package com.smartcampus.platform.quiz.dto;

public class QuizSubmissionResponse {
  private boolean passed;
  private int correctCount;
  private int total;
  private String message;
  private int bonusQueries;

  public QuizSubmissionResponse(
      boolean passed,
      int correctCount,
      int total,
      String message,
      int bonusQueries
  ) {
    this.passed = passed;
    this.correctCount = correctCount;
    this.total = total;
    this.message = message;
    this.bonusQueries = bonusQueries;
  }

  public boolean isPassed() {
    return passed;
  }

  public int getCorrectCount() {
    return correctCount;
  }

  public int getTotal() {
    return total;
  }

  public String getMessage() {
    return message;
  }

  public int getBonusQueries() {
    return bonusQueries;
  }
}
