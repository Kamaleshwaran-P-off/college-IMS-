package com.smartcampus.platform.risk.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class RiskPredictionRequest {
  @NotNull
  private List<@Min(0) @Max(100) Integer> marks;

  @Min(0)
  @Max(100)
  private Double attendance;

  @Min(0)
  @Max(100)
  private Double assignmentCompletion;

  public List<Integer> getMarks() {
    return marks;
  }

  public void setMarks(List<Integer> marks) {
    this.marks = marks;
  }

  public Double getAttendance() {
    return attendance;
  }

  public void setAttendance(Double attendance) {
    this.attendance = attendance;
  }

  public Double getAssignmentCompletion() {
    return assignmentCompletion;
  }

  public void setAssignmentCompletion(Double assignmentCompletion) {
    this.assignmentCompletion = assignmentCompletion;
  }
}
