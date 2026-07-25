package com.smartcampus.platform.coursework.material.dto;

import java.time.LocalDateTime;

public class StudyMaterialResponse {
  private Long id;
  private String title;
  private String description;
  private String department;
  private String className;
  private String uploadedBy;
  private LocalDateTime uploadedAt;
  private LocalDateTime updatedAt;
  private boolean attachmentAvailable;
  private boolean visible;

  public StudyMaterialResponse() {}

  public StudyMaterialResponse(
      Long id,
      String title,
      String description,
      String department,
      String className,
      String uploadedBy,
      LocalDateTime uploadedAt,
      LocalDateTime updatedAt,
      boolean attachmentAvailable,
      boolean visible
  ) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.department = department;
    this.className = className;
    this.uploadedBy = uploadedBy;
    this.uploadedAt = uploadedAt;
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

  public String getDepartment() {
    return department;
  }

  public String getClassName() {
    return className;
  }

  public String getUploadedBy() {
    return uploadedBy;
  }

  public LocalDateTime getUploadedAt() {
    return uploadedAt;
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
