package com.smartcampus.platform.mentormatching.dto;

import com.smartcampus.platform.mentormatching.entity.MentorRequestStatus;
import jakarta.validation.constraints.NotNull;

public class MentorRequestStatusRequest {
  @NotNull
  private Long requestId;

  @NotNull
  private MentorRequestStatus status;

  public Long getRequestId() {
    return requestId;
  }

  public void setRequestId(Long requestId) {
    this.requestId = requestId;
  }

  public MentorRequestStatus getStatus() {
    return status;
  }

  public void setStatus(MentorRequestStatus status) {
    this.status = status;
  }
}
