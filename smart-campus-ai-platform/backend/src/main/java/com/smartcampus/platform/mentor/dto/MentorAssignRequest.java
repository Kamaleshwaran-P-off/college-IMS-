package com.smartcampus.platform.mentor.dto;

import jakarta.validation.constraints.NotNull;

public class MentorAssignRequest {
  @NotNull
  private Long studentId;

  @NotNull
  private Long mentorId;

  private Long assignedByUserId;

  public Long getStudentId() {
    return studentId;
  }

  public void setStudentId(Long studentId) {
    this.studentId = studentId;
  }

  public Long getMentorId() {
    return mentorId;
  }

  public void setMentorId(Long mentorId) {
    this.mentorId = mentorId;
  }

  public Long getAssignedByUserId() {
    return assignedByUserId;
  }

  public void setAssignedByUserId(Long assignedByUserId) {
    this.assignedByUserId = assignedByUserId;
  }
}
