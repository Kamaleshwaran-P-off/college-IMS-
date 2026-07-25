package com.smartcampus.platform.feed.entity;

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
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "career_feed_saves",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "item_id"})
)
public class CareerFeedSave {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id", nullable = false)
  private CareerFeedItem item;

  @Column(nullable = false)
  private LocalDateTime savedAt;

  public CareerFeedSave() {}

  public CareerFeedSave(Long userId, CareerFeedItem item, LocalDateTime savedAt) {
    this.userId = userId;
    this.item = item;
    this.savedAt = savedAt;
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

  public CareerFeedItem getItem() {
    return item;
  }

  public void setItem(CareerFeedItem item) {
    this.item = item;
  }

  public LocalDateTime getSavedAt() {
    return savedAt;
  }

  public void setSavedAt(LocalDateTime savedAt) {
    this.savedAt = savedAt;
  }
}
