package com.smartcampus.platform.quiz.dto;

import com.smartcampus.platform.quiz.entity.QuizCategory;

public class QuizQuestionResponse {
  private Long id;
  private QuizCategory category;
  private String question;
  private String optionA;
  private String optionB;
  private String optionC;
  private String optionD;

  public QuizQuestionResponse(
      Long id,
      QuizCategory category,
      String question,
      String optionA,
      String optionB,
      String optionC,
      String optionD
  ) {
    this.id = id;
    this.category = category;
    this.question = question;
    this.optionA = optionA;
    this.optionB = optionB;
    this.optionC = optionC;
    this.optionD = optionD;
  }

  public Long getId() {
    return id;
  }

  public QuizCategory getCategory() {
    return category;
  }

  public String getQuestion() {
    return question;
  }

  public String getOptionA() {
    return optionA;
  }

  public String getOptionB() {
    return optionB;
  }

  public String getOptionC() {
    return optionC;
  }

  public String getOptionD() {
    return optionD;
  }
}
