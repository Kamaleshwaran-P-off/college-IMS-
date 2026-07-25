package com.smartcampus.platform.learning.dto;

import java.util.List;

public class QuizSubmitResponse {
  private int score;
  private boolean passed;
  private Long nextTopicId;
  private List<String> explanations;

  public QuizSubmitResponse() {}

  public QuizSubmitResponse(int score, boolean passed, Long nextTopicId, List<String> explanations) {
    this.score = score;
    this.passed = passed;
    this.nextTopicId = nextTopicId;
    this.explanations = explanations;
  }

  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

  public boolean isPassed() {
    return passed;
  }

  public void setPassed(boolean passed) {
    this.passed = passed;
  }

  public Long getNextTopicId() {
    return nextTopicId;
  }

  public void setNextTopicId(Long nextTopicId) {
    this.nextTopicId = nextTopicId;
  }

  public List<String> getExplanations() {
    return explanations;
  }

  public void setExplanations(List<String> explanations) {
    this.explanations = explanations;
  }
}
