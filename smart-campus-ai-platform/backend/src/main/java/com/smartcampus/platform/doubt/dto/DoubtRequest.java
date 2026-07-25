package com.smartcampus.platform.doubt.dto;

import com.smartcampus.platform.doubt.entity.DoubtStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DoubtRequest {
  @NotNull
  private Long userId;

  private Long assignmentId;

  @NotBlank
  private String title;

  private String description;

  private DoubtStatus status;

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public Long getAssignmentId() {
    return assignmentId;
  }

  public void setAssignmentId(Long assignmentId) {
    this.assignmentId = assignmentId;
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

  public DoubtStatus getStatus() {
    return status;
  }

  public void setStatus(DoubtStatus status) {
    this.status = status;
  }
}
