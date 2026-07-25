package com.smartcampus.platform.leave.dto;

import jakarta.validation.constraints.NotNull;

public class LeaveDecisionRequest {
  @NotNull
  private Long approverUserId;

  private String note;

  public Long getApproverUserId() {
    return approverUserId;
  }

  public void setApproverUserId(Long approverUserId) {
    this.approverUserId = approverUserId;
  }

  public String getNote() {
    return note;
  }

  public void setNote(String note) {
    this.note = note;
  }
}
