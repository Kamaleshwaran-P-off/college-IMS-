package com.smartcampus.platform.planner.dto;

import java.time.LocalDate;

public class StudyStreakResponse {
  private int currentStreak;
  private int longestStreak;
  private LocalDate lastCompletedDate;

  public StudyStreakResponse(int currentStreak, int longestStreak, LocalDate lastCompletedDate) {
    this.currentStreak = currentStreak;
    this.longestStreak = longestStreak;
    this.lastCompletedDate = lastCompletedDate;
  }

  public int getCurrentStreak() {
    return currentStreak;
  }

  public int getLongestStreak() {
    return longestStreak;
  }

  public LocalDate getLastCompletedDate() {
    return lastCompletedDate;
  }
}
