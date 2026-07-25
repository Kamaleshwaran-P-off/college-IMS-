package com.smartcampus.platform.planner.dto;

import java.time.LocalDate;
import java.util.List;

import com.smartcampus.platform.planner.entity.RiskLevel;
import jakarta.validation.constraints.NotNull;

public class StudyPlanRequest {
  @NotNull
  private Long userId;

  private LocalDate weekStart;

  private List<StudyMarkRequest> marks;

  private List<String> weakSubjects;

  private List<String> assignments;

  @NotNull
  private RiskLevel riskLevel;

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public LocalDate getWeekStart() {
    return weekStart;
  }

  public void setWeekStart(LocalDate weekStart) {
    this.weekStart = weekStart;
  }

  public List<StudyMarkRequest> getMarks() {
    return marks;
  }

  public void setMarks(List<StudyMarkRequest> marks) {
    this.marks = marks;
  }

  public List<String> getWeakSubjects() {
    return weakSubjects;
  }

  public void setWeakSubjects(List<String> weakSubjects) {
    this.weakSubjects = weakSubjects;
  }

  public List<String> getAssignments() {
    return assignments;
  }

  public void setAssignments(List<String> assignments) {
    this.assignments = assignments;
  }

  public RiskLevel getRiskLevel() {
    return riskLevel;
  }

  public void setRiskLevel(RiskLevel riskLevel) {
    this.riskLevel = riskLevel;
  }
}
