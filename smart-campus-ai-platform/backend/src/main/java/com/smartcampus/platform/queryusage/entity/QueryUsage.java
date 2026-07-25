package com.smartcampus.platform.queryusage.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.smartcampus.platform.auth.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "query_usage")
public class QueryUsage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @NotBlank
  @Column(nullable = false)
  private String queryType;

  private Integer tokensUsed;

  @Column(precision = 12, scale = 4)
  private BigDecimal cost;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public QueryUsage() {}

  public QueryUsage(User user, String queryType, Integer tokensUsed, BigDecimal cost) {
    this.user = user;
    this.queryType = queryType;
    this.tokensUsed = tokensUsed;
    this.cost = cost;
  }

  @PrePersist
  public void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }

  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
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

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
