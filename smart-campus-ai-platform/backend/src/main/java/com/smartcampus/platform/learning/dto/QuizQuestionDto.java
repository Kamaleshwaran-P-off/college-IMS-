package com.smartcampus.platform.learning.dto;

import java.util.List;

public class QuizQuestionDto {
  private int id;
  private String type;
  private String question;
  private List<String> options;
  private String answer;
  private String explanation;

  public QuizQuestionDto() {}

  public QuizQuestionDto(int id, String type, String question, List<String> options, String answer, String explanation) {
    this.id = id;
    this.type = type;
    this.question = question;
    this.options = options;
    this.answer = answer;
    this.explanation = explanation;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getQuestion() {
    return question;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public List<String> getOptions() {
    return options;
  }

  public void setOptions(List<String> options) {
    this.options = options;
  }

  public String getAnswer() {
    return answer;
  }

  public void setAnswer(String answer) {
    this.answer = answer;
  }

  public String getExplanation() {
    return explanation;
  }

  public void setExplanation(String explanation) {
    this.explanation = explanation;
  }
}
