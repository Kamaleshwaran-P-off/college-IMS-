package com.smartcampus.platform.learning.dto;

import java.util.List;

public class QuizResponse {
  private Long topicId;
  private List<QuizQuestionDto> questions;

  public QuizResponse() {}

  public QuizResponse(Long topicId, List<QuizQuestionDto> questions) {
    this.topicId = topicId;
    this.questions = questions;
  }

  public Long getTopicId() {
    return topicId;
  }

  public void setTopicId(Long topicId) {
    this.topicId = topicId;
  }

  public List<QuizQuestionDto> getQuestions() {
    return questions;
  }

  public void setQuestions(List<QuizQuestionDto> questions) {
    this.questions = questions;
  }
}
