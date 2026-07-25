package com.smartcampus.platform.mentor.dto;

public class MentorAnalyticsResponse {
  private Long mentorId;
  private String mentorName;
  private String mentorCode;
  private String department;
  private long menteeCount;
  private long totalDecisions;
  private Double avgApprovalHours;

  public MentorAnalyticsResponse(
      Long mentorId,
      String mentorName,
      String mentorCode,
      String department,
      long menteeCount,
      long totalDecisions,
      Double avgApprovalHours
  ) {
    this.mentorId = mentorId;
    this.mentorName = mentorName;
    this.mentorCode = mentorCode;
    this.department = department;
    this.menteeCount = menteeCount;
    this.totalDecisions = totalDecisions;
    this.avgApprovalHours = avgApprovalHours;
  }

  public Long getMentorId() {
    return mentorId;
  }

  public String getMentorName() {
    return mentorName;
  }

  public String getMentorCode() {
    return mentorCode;
  }

  public String getDepartment() {
    return department;
  }

  public long getMenteeCount() {
    return menteeCount;
  }

  public long getTotalDecisions() {
    return totalDecisions;
  }

  public Double getAvgApprovalHours() {
    return avgApprovalHours;
  }
}
