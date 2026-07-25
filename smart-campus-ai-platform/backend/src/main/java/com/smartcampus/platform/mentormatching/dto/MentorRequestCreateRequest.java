package com.smartcampus.platform.mentormatching.dto;

import jakarta.validation.constraints.NotNull;

public class MentorRequestCreateRequest {
  @NotNull
  private Long mentorId;

  private String message;

  public Long getMentorId() {
    return mentorId;
  }

  public void setMentorId(Long mentorId) {
    this.mentorId = mentorId;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }
}
