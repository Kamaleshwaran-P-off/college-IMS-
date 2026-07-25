package com.smartcampus.platform.leave.dto;

import java.time.LocalDate;

import com.smartcampus.platform.leave.entity.LeaveType;
import jakarta.validation.constraints.NotNull;

public class LeaveCreateRequest {
  @NotNull
  private Long userId;

  @NotNull
  private LeaveType type;

  @NotNull
  private LocalDate startDate;

  private LocalDate endDate;

  private String reason;

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public LeaveType getType() {
    return type;
  }

  public void setType(LeaveType type) {
    this.type = type;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }
}
