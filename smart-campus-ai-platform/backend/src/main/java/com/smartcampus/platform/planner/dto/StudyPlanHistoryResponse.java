package com.smartcampus.platform.planner.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.smartcampus.platform.planner.entity.RiskLevel;

public class StudyPlanHistoryResponse {
  private Long id;
  private LocalDate weekStart;
  private RiskLevel riskLevel;
  private LocalDateTime createdAt;
  private long completedTasks;
  private long totalTasks;

  public StudyPlanHistoryResponse(
      Long id,
      LocalDate weekStart,
      RiskLevel riskLevel,
      LocalDateTime createdAt,
      long completedTasks,
      long totalTasks
  ) {
    this.id = id;
    this.weekStart = weekStart;
    this.riskLevel = riskLevel;
    this.createdAt = createdAt;
    this.completedTasks = completedTasks;
    this.totalTasks = totalTasks;
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

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public long getCompletedTasks() {
    return completedTasks;
  }

  public long getTotalTasks() {
    return totalTasks;
  }
}
