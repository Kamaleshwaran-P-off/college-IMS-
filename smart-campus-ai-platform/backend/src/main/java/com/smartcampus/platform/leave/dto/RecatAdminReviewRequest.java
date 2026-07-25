package com.smartcampus.platform.leave.dto;

import com.smartcampus.platform.leave.entity.LeaveStatus;
import jakarta.validation.constraints.NotNull;

public class RecatAdminReviewRequest {
  @NotNull
  private Long requestId;

  @NotNull
  private LeaveStatus status;

  private String adminRemarks;

  public Long getRequestId() {
    return requestId;
  }

  public void setRequestId(Long requestId) {
    this.requestId = requestId;
  }

  public LeaveStatus getStatus() {
    return status;
  }

  public void setStatus(LeaveStatus status) {
    this.status = status;
  }

  public String getAdminRemarks() {
    return adminRemarks;
  }

  public void setAdminRemarks(String adminRemarks) {
    this.adminRemarks = adminRemarks;
  }
}
