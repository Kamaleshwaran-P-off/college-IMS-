package com.smartcampus.platform.happenstance.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "happenstance_clicks")
public class HappenstanceClick {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Long userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "opportunity_id", nullable = false)
  private HappenstanceOpportunity opportunity;

  @Column(nullable = false)
  private LocalDateTime clickedAt;

  public HappenstanceClick() {}

  public HappenstanceClick(Long userId, HappenstanceOpportunity opportunity, LocalDateTime clickedAt) {
    this.userId = userId;
    this.opportunity = opportunity;
    this.clickedAt = clickedAt;
  }

  public Long getId() {
    return id;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public HappenstanceOpportunity getOpportunity() {
    return opportunity;
  }

  public void setOpportunity(HappenstanceOpportunity opportunity) {
    this.opportunity = opportunity;
  }

  public LocalDateTime getClickedAt() {
    return clickedAt;
  }

  public void setClickedAt(LocalDateTime clickedAt) {
    this.clickedAt = clickedAt;
  }
}
