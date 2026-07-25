package com.smartcampus.platform.queryusage.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class QueryUsageRequest {
  @NotNull
  private Long userId;

  @NotBlank
  private String queryType;

  private Integer tokensUsed;
  private BigDecimal cost;

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getQueryType() {
    return queryType;
  }

  public void setQueryType(String queryType) {
    this.queryType = queryType;
  }

  public Integer getTokensUsed() {
    return tokensUsed;
  }

  public void setTokensUsed(Integer tokensUsed) {
    this.tokensUsed = tokensUsed;
  }

  public BigDecimal getCost() {
    return cost;
  }

  public void setCost(BigDecimal cost) {
    this.cost = cost;
  }
}
