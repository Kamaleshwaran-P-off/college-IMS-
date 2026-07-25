package com.smartcampus.platform.aiquiz.dto;

import java.util.List;

public class AiQuizResponse {
  private Long id;
  private String title;
  private String department;
  private String className;
  private boolean visible;
  private Integer durationMinutes;
  private List<AiQuizQuestionView> questions;

  public AiQuizResponse() {}

  public AiQuizResponse(
      Long id,
      String title,
      String department,
      String className,
      boolean visible,
      Integer durationMinutes,
      List<AiQuizQuestionView> questions
  ) {
    this.id = id;
    this.title = title;
    this.department = department;
    this.className = className;
    this.visible = visible;
    this.durationMinutes = durationMinutes;
    this.questions = questions;
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDepartment() {
    return department;
  }

  public String getClassName() {
    return className;
  }

  public boolean isVisible() {
    return visible;
  }

  public Integer getDurationMinutes() {
    return durationMinutes;
  }

  public List<AiQuizQuestionView> getQuestions() {
    return questions;
  }

  public static class AiQuizQuestionView {
    private int index;
    private String type;
    private String question;
    private List<String> options;

    public AiQuizQuestionView() {}

    public AiQuizQuestionView(int index, String type, String question, List<String> options) {
      this.index = index;
      this.type = type;
      this.question = question;
      this.options = options;
    }

    public int getIndex() {
      return index;
    }

    public String getType() {
      return type;
    }

    public String getQuestion() {
      return question;
    }

    public List<String> getOptions() {
      return options;
    }
  }
}
