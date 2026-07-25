package com.smartcampus.platform.planner.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "study_tasks")
public class StudyTask {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "plan_id", nullable = false)
  private StudyPlan plan;

  @Column(nullable = false)
  private int dayOrder;

  @Column(nullable = false)
  private String dayLabel;

  @Column(nullable = false)
  private String title;

  @Lob
  private String details;

  @Column(nullable = false)
  private boolean completed;

  private LocalDateTime completedAt;

  private LocalDateTime reminderAt;

  @Column(nullable = false)
  private boolean reminderSent;

  private LocalDateTime reminderSentAt;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public StudyTask() {}

  public StudyTask(StudyPlan plan, int dayOrder, String dayLabel, String title, String details) {
    this.plan = plan;
    this.dayOrder = dayOrder;
    this.dayLabel = dayLabel;
    this.title = title;
    this.details = details;
    this.completed = false;
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

  public StudyPlan getPlan() {
    return plan;
  }

  public int getDayOrder() {
    return dayOrder;
  }

  public String getDayLabel() {
    return dayLabel;
  }

  public String getTitle() {
    return title;
  }

  public String getDetails() {
    return details;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
    this.completedAt = completed ? LocalDateTime.now() : null;
  }

  public LocalDateTime getCompletedAt() {
    return completedAt;
  }

  public LocalDateTime getReminderAt() {
    return reminderAt;
  }

  public void setReminderAt(LocalDateTime reminderAt) {
    this.reminderAt = reminderAt;
    this.reminderSent = false;
    this.reminderSentAt = null;
  }

  public boolean isReminderSent() {
    return reminderSent;
  }

  public void setReminderSent(boolean reminderSent) {
    this.reminderSent = reminderSent;
    this.reminderSentAt = reminderSent ? LocalDateTime.now() : null;
  }

  public LocalDateTime getReminderSentAt() {
    return reminderSentAt;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
