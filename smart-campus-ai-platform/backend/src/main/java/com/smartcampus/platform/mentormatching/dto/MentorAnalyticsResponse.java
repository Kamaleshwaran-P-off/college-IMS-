package com.smartcampus.platform.mentormatching.dto;

public class MentorAnalyticsResponse {
  private Long mentorId;
  private String mentorName;
  private String department;
  private long totalMatches;
  private Double averageScore;
  private long pendingRequests;
  private long acceptedRequests;
  private long rejectedRequests;

  public MentorAnalyticsResponse(
      Long mentorId,
      String mentorName,
      String department,
      long totalMatches,
      Double averageScore,
      long pendingRequests,
      long acceptedRequests,
      long rejectedRequests
  ) {
    this.mentorId = mentorId;
    this.mentorName = mentorName;
    this.department = department;
    this.totalMatches = totalMatches;
    this.averageScore = averageScore;
    this.pendingRequests = pendingRequests;
    this.acceptedRequests = acceptedRequests;
    this.rejectedRequests = rejectedRequests;
  }

  public Long getMentorId() {
    return mentorId;
  }

  public String getMentorName() {
    return mentorName;
  }

  public String getDepartment() {
    return department;
  }

  public long getTotalMatches() {
    return totalMatches;
  }

  public Double getAverageScore() {
    return averageScore;
  }

  public long getPendingRequests() {
    return pendingRequests;
  }

  public long getAcceptedRequests() {
    return acceptedRequests;
  }

  public long getRejectedRequests() {
    return rejectedRequests;
  }
}
