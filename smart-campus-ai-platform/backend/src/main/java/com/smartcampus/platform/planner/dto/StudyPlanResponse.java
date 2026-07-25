package com.smartcampus.platform.planner.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.smartcampus.platform.planner.entity.RiskLevel;

public class StudyPlanResponse {
  private Long id;
  private LocalDate weekStart;
  private RiskLevel riskLevel;
  private String planText;
  private List<StudyTaskResponse> tasks;
  private LocalDateTime createdAt;

  public StudyPlanResponse(
      Long id,
      LocalDate weekStart,
      RiskLevel riskLevel,
      String planText,
      List<StudyTaskResponse> tasks,
      LocalDateTime createdAt
  ) {
    this.id = id;
    this.weekStart = weekStart;
    this.riskLevel = riskLevel;
    this.planText = planText;
    this.tasks = tasks;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public LocalDate getWeekStart() {
    return weekStart;
  }

  public RiskLevel getRiskLevel() {
    return riskLevel;
  }

  public String getPlanText() {
    return planText;
  }

  public List<StudyTaskResponse> getTasks() {
    return tasks;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
