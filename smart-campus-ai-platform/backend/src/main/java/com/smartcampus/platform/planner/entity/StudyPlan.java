package com.smartcampus.platform.planner.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.smartcampus.platform.auth.entity.User;
import jakarta.persistence.*;

@Entity
@Table(name = "study_plans")
public class StudyPlan {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(nullable = false)
  private LocalDate weekStart;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private RiskLevel riskLevel;

  @Lob
  private String marksJson;

  @Lob
  private String weakSubjectsJson;

  @Lob
  private String assignmentsJson;

  @Lob
  private String planText;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public StudyPlan() {}

  public StudyPlan(
      User user,
      LocalDate weekStart,
      RiskLevel riskLevel,
      String marksJson,
      String weakSubjectsJson,
      String assignmentsJson,
      String planText
  ) {
    this.user = user;
    this.weekStart = weekStart;
    this.riskLevel = riskLevel;
    this.marksJson = marksJson;
    this.weakSubjectsJson = weakSubjectsJson;
    this.assignmentsJson = assignmentsJson;
    this.planText = planText;
  }

  @PrePersist
  public void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }

  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public LocalDate getWeekStart() {
    return weekStart;
  }

  public RiskLevel getRiskLevel() {
    return riskLevel;
  }

  public String getMarksJson() {
    return marksJson;
  }

  public String getWeakSubjectsJson() {
    return weakSubjectsJson;
  }

  public String getAssignmentsJson() {
    return assignmentsJson;
  }

  public String getPlanText() {
    return planText;
  }

  public void setPlanText(String planText) {
    this.planText = planText;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
