package com.smartcampus.platform.coursework.assignment.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CourseAssignmentResponse {
  private Long id;
  private String title;
  private String description;
  private LocalDate dueDate;
  private String department;
  private String className;
  private String createdBy;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private boolean attachmentAvailable;
  private boolean visible;

  public CourseAssignmentResponse() {}

  public CourseAssignmentResponse(
      Long id,
      String title,
      String description,
      LocalDate dueDate,
      String department,
      String className,
      String createdBy,
      LocalDateTime createdAt,
      LocalDateTime updatedAt,
      boolean attachmentAvailable,
      boolean visible
  ) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.dueDate = dueDate;
    this.department = department;
    this.className = className;
    this.createdBy = createdBy;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.attachmentAvailable = attachmentAvailable;
    this.visible = visible;
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public String getDepartment() {
    return department;
  }

  public String getClassName() {
    return className;
  }

  public String getCreatedBy() {
    return createdBy;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public boolean isAttachmentAvailable() {
    return attachmentAvailable;
  }

  public boolean isVisible() {
    return visible;
  }
}
