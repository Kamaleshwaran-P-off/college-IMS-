package com.smartcampus.platform.aiquiz.dto;

public class AiQuizUpdateRequest {
  private String title;
  private String syllabus;
  private String department;
  private String className;
  private Integer durationMinutes;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSyllabus() {
    return syllabus;
  }

  public void setSyllabus(String syllabus) {
    this.syllabus = syllabus;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public Integer getDurationMinutes() {
    return durationMinutes;
  }

  public void setDurationMinutes(Integer durationMinutes) {
    this.durationMinutes = durationMinutes;
  }
}
