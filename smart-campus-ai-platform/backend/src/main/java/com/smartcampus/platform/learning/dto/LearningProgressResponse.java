package com.smartcampus.platform.learning.dto;

import java.util.List;

public class LearningProgressResponse {
  private int completedTopics;
  private int totalTopics;
  private double completionPercent;
  private int points;
  private int streak;
  private String badges;
  private List<Integer> recentScores;

  public LearningProgressResponse() {}

  public LearningProgressResponse(int completedTopics, int totalTopics, double completionPercent, int points, int streak, String badges, List<Integer> recentScores) {
    this.completedTopics = completedTopics;
    this.totalTopics = totalTopics;
    this.completionPercent = completionPercent;
    this.points = points;
    this.streak = streak;
    this.badges = badges;
    this.recentScores = recentScores;
  }

  public int getCompletedTopics() {
    return completedTopics;
  }

  public void setCompletedTopics(int completedTopics) {
    this.completedTopics = completedTopics;
  }

  public int getTotalTopics() {
    return totalTopics;
  }

  public void setTotalTopics(int totalTopics) {
    this.totalTopics = totalTopics;
  }

  public double getCompletionPercent() {
    return completionPercent;
  }

  public void setCompletionPercent(double completionPercent) {
    this.completionPercent = completionPercent;
  }

  public int getPoints() {
    return points;
  }

  public void setPoints(int points) {
    this.points = points;
  }

  public int getStreak() {
    return streak;
  }

  public void setStreak(int streak) {
    this.streak = streak;
  }

  public String getBadges() {
    return badges;
  }

  public void setBadges(String badges) {
    this.badges = badges;
  }

  public List<Integer> getRecentScores() {
    return recentScores;
  }

  public void setRecentScores(List<Integer> recentScores) {
    this.recentScores = recentScores;
  }
}
