package com.smartcampus.platform.doubt.dto;

public class LeaderboardEntry {
  private Long userId;
  private String name;
  private long acceptedCount;

  public LeaderboardEntry(Long userId, String name, long acceptedCount) {
    this.userId = userId;
    this.name = name;
    this.acceptedCount = acceptedCount;
  }

  public Long getUserId() {
    return userId;
  }

  public String getName() {
    return name;
  }

  public long getAcceptedCount() {
    return acceptedCount;
  }
}
