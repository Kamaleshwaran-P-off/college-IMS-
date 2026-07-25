package com.smartcampus.platform.happenstance.dto;

import java.util.List;

public class HappenstanceAdminAnalyticsResponse {
  private List<HappenstanceDomainStat> topClickedDomains;
  private List<HappenstanceDomainStat> mostSavedDomains;
  private List<HappenstanceOpportunityStat> mostSavedOpportunities;
  private long totalClicks;
  private long totalSaves;
  private int activeUsers;
  private double averageSerendipityScore;

  public HappenstanceAdminAnalyticsResponse(
      List<HappenstanceDomainStat> topClickedDomains,
      List<HappenstanceDomainStat> mostSavedDomains,
      List<HappenstanceOpportunityStat> mostSavedOpportunities,
      long totalClicks,
      long totalSaves,
      int activeUsers,
      double averageSerendipityScore
  ) {
    this.topClickedDomains = topClickedDomains;
    this.mostSavedDomains = mostSavedDomains;
    this.mostSavedOpportunities = mostSavedOpportunities;
    this.totalClicks = totalClicks;
    this.totalSaves = totalSaves;
    this.activeUsers = activeUsers;
    this.averageSerendipityScore = averageSerendipityScore;
  }

  public List<HappenstanceDomainStat> getTopClickedDomains() {
    return topClickedDomains;
  }

  public List<HappenstanceDomainStat> getMostSavedDomains() {
    return mostSavedDomains;
  }

  public List<HappenstanceOpportunityStat> getMostSavedOpportunities() {
    return mostSavedOpportunities;
  }

  public long getTotalClicks() {
    return totalClicks;
  }

  public long getTotalSaves() {
    return totalSaves;
  }

  public int getActiveUsers() {
    return activeUsers;
  }

  public double getAverageSerendipityScore() {
    return averageSerendipityScore;
  }
}
