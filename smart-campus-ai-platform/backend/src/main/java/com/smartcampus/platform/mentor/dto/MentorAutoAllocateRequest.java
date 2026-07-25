package com.smartcampus.platform.mentor.dto;

public class MentorAutoAllocateRequest {
  private Long assignedByUserId;

  public Long getAssignedByUserId() {
    return assignedByUserId;
  }

  public void setAssignedByUserId(Long assignedByUserId) {
    this.assignedByUserId = assignedByUserId;
  }
}
