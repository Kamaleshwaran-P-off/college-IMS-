package com.smartcampus.platform.happenstance.dto;

import java.util.List;

public class HappenstanceAnalyticsResponse {
  private List<HappenstanceDomainStat> topClickedDomains;
  private List<HappenstanceDomainStat> mostSavedDomains;
  private List<HappenstanceOpportunityStat> mostSavedOpportunities;
  private long totalClicks;
  private long totalSaves;
  private HappenstanceSerendipityScore serendipityScore;

  public HappenstanceAnalyticsResponse(
      List<HappenstanceDomainStat> topClickedDomains,
      List<HappenstanceDomainStat> mostSavedDomains,
      List<HappenstanceOpportunityStat> mostSavedOpportunities,
      long totalClicks,
      long totalSaves,
      HappenstanceSerendipityScore serendipityScore
  ) {
    this.topClickedDomains = topClickedDomains;
    this.mostSavedDomains = mostSavedDomains;
    this.mostSavedOpportunities = mostSavedOpportunities;
    this.totalClicks = totalClicks;
    this.totalSaves = totalSaves;
    this.serendipityScore = serendipityScore;
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

  public HappenstanceSerendipityScore getSerendipityScore() {
    return serendipityScore;
  }
}
