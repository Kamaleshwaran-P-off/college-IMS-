package com.smartcampus.platform.assignmentplanner.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "planner_tasks")
public class ScheduledTask {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "assignment_id", nullable = false)
  private PlannerAssignment assignment;

  @Column(nullable = false)
  private Long studentId;

  @Column(nullable = false)
  private LocalDate taskDate;

  @Column(length = 255)
  private String taskDetail;

  private Double hours;

  private boolean completed;

  private LocalDateTime createdAt;

  public ScheduledTask() {}

  public ScheduledTask(
      PlannerAssignment assignment,
      Long studentId,
      LocalDate taskDate,
      String taskDetail,
      Double hours,
      boolean completed,
      LocalDateTime createdAt
  ) {
    this.assignment = assignment;
    this.studentId = studentId;
    this.taskDate = taskDate;
    this.taskDetail = taskDetail;
    this.hours = hours;
    this.completed = completed;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public PlannerAssignment getAssignment() {
    return assignment;
  }

  public void setAssignment(PlannerAssignment assignment) {
    this.assignment = assignment;
  }

  public Long getStudentId() {
    return studentId;
  }

  public void setStudentId(Long studentId) {
    this.studentId = studentId;
  }

  public LocalDate getTaskDate() {
    return taskDate;
  }

  public void setTaskDate(LocalDate taskDate) {
    this.taskDate = taskDate;
  }

  public String getTaskDetail() {
    return taskDetail;
  }

  public void setTaskDetail(String taskDetail) {
    this.taskDetail = taskDetail;
  }

  public Double getHours() {
    return hours;
  }

  public void setHours(Double hours) {
    this.hours = hours;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
