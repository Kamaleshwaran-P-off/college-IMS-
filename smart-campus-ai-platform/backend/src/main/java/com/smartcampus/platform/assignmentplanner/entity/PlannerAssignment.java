package com.smartcampus.platform.assignmentplanner.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.smartcampus.platform.staff.entity.Staff;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "planner_assignments")
public class PlannerAssignment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(nullable = false)
  private String title;

  @Lob
  private String description;

  private LocalDate deadline;

  private Double estimatedHours;

  @NotNull
  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AssignmentTargetType targetType;

  private Long targetStudentId;

  @Column(length = 100)
  private String targetDepartment;

  @Column(length = 20)
  private String targetSection;

  @ManyToOne(optional = false)
  @JoinColumn(name = "staff_id", nullable = false)
  private Staff createdBy;

  private LocalDateTime createdAt;

  public PlannerAssignment() {}

  public PlannerAssignment(
      String title,
      String description,
      LocalDate deadline,
      Double estimatedHours,
      AssignmentTargetType targetType,
      Long targetStudentId,
      String targetDepartment,
      String targetSection,
      Staff createdBy,
      LocalDateTime createdAt
  ) {
    this.title = title;
    this.description = description;
    this.deadline = deadline;
    this.estimatedHours = estimatedHours;
    this.targetType = targetType;
    this.targetStudentId = targetStudentId;
    this.targetDepartment = targetDepartment;
    this.targetSection = targetSection;
    this.createdBy = createdBy;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDate getDeadline() {
    return deadline;
  }

  public void setDeadline(LocalDate deadline) {
    this.deadline = deadline;
  }

  public Double getEstimatedHours() {
    return estimatedHours;
  }

  public void setEstimatedHours(Double estimatedHours) {
    this.estimatedHours = estimatedHours;
  }

  public AssignmentTargetType getTargetType() {
    return targetType;
  }

  public void setTargetType(AssignmentTargetType targetType) {
    this.targetType = targetType;
  }

  public Long getTargetStudentId() {
    return targetStudentId;
  }

  public void setTargetStudentId(Long targetStudentId) {
    this.targetStudentId = targetStudentId;
  }

  public String getTargetDepartment() {
    return targetDepartment;
  }

  public void setTargetDepartment(String targetDepartment) {
    this.targetDepartment = targetDepartment;
  }

  public String getTargetSection() {
    return targetSection;
  }

  public void setTargetSection(String targetSection) {
    this.targetSection = targetSection;
  }

  public Staff getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(Staff createdBy) {
    this.createdBy = createdBy;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
