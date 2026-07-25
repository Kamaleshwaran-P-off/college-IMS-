package com.smartcampus.platform.happenstance.dto;

public class HappenstanceSaveResponse {
  private Long opportunityId;
  private boolean saved;

  public HappenstanceSaveResponse(Long opportunityId, boolean saved) {
    this.opportunityId = opportunityId;
    this.saved = saved;
  }

  public Long getOpportunityId() {
    return opportunityId;
  }

  public boolean isSaved() {
    return saved;
  }
}
