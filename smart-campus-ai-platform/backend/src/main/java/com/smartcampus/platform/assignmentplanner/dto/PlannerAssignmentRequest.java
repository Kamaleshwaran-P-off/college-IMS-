package com.smartcampus.platform.assignmentplanner.dto;

import java.time.LocalDate;

import com.smartcampus.platform.assignmentplanner.entity.AssignmentTargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PlannerAssignmentRequest {
  @NotBlank
  private String title;

  private String description;

  private LocalDate deadline;

  private Double estimatedHours;

  @NotNull
  private AssignmentTargetType targetType;

  private Long targetStudentId;

  private String targetDepartment;

  private String targetSection;

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
}
