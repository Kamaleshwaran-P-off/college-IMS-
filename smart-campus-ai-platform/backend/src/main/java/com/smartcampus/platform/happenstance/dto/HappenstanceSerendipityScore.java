package com.smartcampus.platform.happenstance.dto;

public class HappenstanceSerendipityScore {
  private int score;
  private int uniqueDomains;
  private int totalInteractions;
  private int outOfComfortInteractions;

  public HappenstanceSerendipityScore(
      int score,
      int uniqueDomains,
      int totalInteractions,
      int outOfComfortInteractions
  ) {
    this.score = score;
    this.uniqueDomains = uniqueDomains;
    this.totalInteractions = totalInteractions;
    this.outOfComfortInteractions = outOfComfortInteractions;
  }

  public int getScore() {
    return score;
  }

  public int getUniqueDomains() {
    return uniqueDomains;
  }

  public int getTotalInteractions() {
    return totalInteractions;
  }

  public int getOutOfComfortInteractions() {
    return outOfComfortInteractions;
  }
}
