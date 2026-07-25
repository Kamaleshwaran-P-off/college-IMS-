package com.smartcampus.platform.notification.broadcast.dto;

import jakarta.validation.constraints.NotBlank;

public class NotificationCreateRequest {
  @NotBlank
  private String title;

  @NotBlank
  private String message;

  @NotBlank
  private String targetRole;

  private String department;
  private String className;

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public String getTargetRole() {
    return targetRole;
  }

  public void setTargetRole(String targetRole) {
    this.targetRole = targetRole;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }
}
