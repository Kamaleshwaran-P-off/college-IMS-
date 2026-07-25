package com.smartcampus.platform.risk.dto;

public class RiskPredictionResponse {
  private String risk;
  private double score;

  public RiskPredictionResponse(String risk, double score) {
    this.risk = risk;
    this.score = score;
  }

  public String getRisk() {
    return risk;
  }

  public double getScore() {
    return score;
  }
}
