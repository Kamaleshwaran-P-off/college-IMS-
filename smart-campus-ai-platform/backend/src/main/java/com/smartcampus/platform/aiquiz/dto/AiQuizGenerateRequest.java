package com.smartcampus.platform.aiquiz.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AiQuizGenerateRequest {
  @NotBlank
  private String syllabus;

  @NotNull
  private Integer questionCount;

  @NotNull
  private List<String> questionTypes;

  private String title;

  private String className;

  private String department;

  @NotNull
  private Integer durationMinutes;

  public String getSyllabus() {
    return syllabus;
  }

  public void setSyllabus(String syllabus) {
    this.syllabus = syllabus;
  }

  public Integer getQuestionCount() {
    return questionCount;
  }

  public void setQuestionCount(Integer questionCount) {
    this.questionCount = questionCount;
  }

  public List<String> getQuestionTypes() {
    return questionTypes;
  }

  public void setQuestionTypes(List<String> questionTypes) {
    this.questionTypes = questionTypes;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public Integer getDurationMinutes() {
    return durationMinutes;
  }

  public void setDurationMinutes(Integer durationMinutes) {
    this.durationMinutes = durationMinutes;
  }
}
