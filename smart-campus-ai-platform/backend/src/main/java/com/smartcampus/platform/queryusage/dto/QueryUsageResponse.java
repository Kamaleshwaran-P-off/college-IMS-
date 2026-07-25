package com.smartcampus.platform.queryusage.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class QueryUsageResponse {
  private Long id;
  private Long userId;
  private String queryType;
  private Integer tokensUsed;
  private BigDecimal cost;
  private LocalDateTime createdAt;

  public QueryUsageResponse(
      Long id,
      Long userId,
      String queryType,
      Integer tokensUsed,
      BigDecimal cost,
      LocalDateTime createdAt
  ) {
    this.id = id;
    this.userId = userId;
    this.queryType = queryType;
    this.tokensUsed = tokensUsed;
    this.cost = cost;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public Long getUserId() {
    return userId;
  }

  public String getQueryType() {
    return queryType;
  }

  public Integer getTokensUsed() {
    return tokensUsed;
  }

  public BigDecimal getCost() {
    return cost;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
