package com.smartcampus.platform.coursework.assignment.dto;

import jakarta.validation.constraints.NotNull;

public class AssignmentGradeRequest {
  @NotNull
  private Double marks;

  private String feedback;

  public AssignmentGradeRequest() {}

  public AssignmentGradeRequest(Double marks, String feedback) {
    this.marks = marks;
    this.feedback = feedback;
  }

  public Double getMarks() {
    return marks;
  }

  public void setMarks(Double marks) {
    this.marks = marks;
  }

  public String getFeedback() {
    return feedback;
  }

  public void setFeedback(String feedback) {
    this.feedback = feedback;
  }
}
