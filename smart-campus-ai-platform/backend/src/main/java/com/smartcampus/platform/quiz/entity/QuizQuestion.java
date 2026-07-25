package com.smartcampus.platform.quiz.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "quiz_questions")
public class QuizQuestion {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private QuizCategory category;

  @NotBlank
  @Column(nullable = false)
  private String question;

  @NotBlank
  @Column(nullable = false)
  private String optionA;

  @NotBlank
  @Column(nullable = false)
  private String optionB;

  @NotBlank
  @Column(nullable = false)
  private String optionC;

  @NotBlank
  @Column(nullable = false)
  private String optionD;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private QuizOption correctOption;

  public QuizQuestion() {}

  public QuizQuestion(
      QuizCategory category,
      String question,
      String optionA,
      String optionB,
      String optionC,
      String optionD,
      QuizOption correctOption
  ) {
    this.category = category;
    this.question = question;
    this.optionA = optionA;
    this.optionB = optionB;
    this.optionC = optionC;
    this.optionD = optionD;
    this.correctOption = correctOption;
  }

  public Long getId() {
    return id;
  }

  public QuizCategory getCategory() {
    return category;
  }

  public void setCategory(QuizCategory category) {
    this.category = category;
  }

  public String getQuestion() {
    return question;
  }

  public void setQuestion(String question) {
    this.question = question;
  }

  public String getOptionA() {
    return optionA;
  }

  public void setOptionA(String optionA) {
    this.optionA = optionA;
  }

  public String getOptionB() {
    return optionB;
  }

  public void setOptionB(String optionB) {
    this.optionB = optionB;
  }

  public String getOptionC() {
    return optionC;
  }

  public void setOptionC(String optionC) {
    this.optionC = optionC;
  }

  public String getOptionD() {
    return optionD;
  }

  public void setOptionD(String optionD) {
    this.optionD = optionD;
  }

  public QuizOption getCorrectOption() {
    return correctOption;
  }

  public void setCorrectOption(QuizOption correctOption) {
    this.correctOption = correctOption;
  }
}
