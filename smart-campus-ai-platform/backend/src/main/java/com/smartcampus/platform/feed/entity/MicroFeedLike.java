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
    name = "micro_feed_likes",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "item_id"})
)
public class MicroFeedLike {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "item_id", nullable = false)
  private MicroFeedItem item;

  @Column(nullable = false)
  private LocalDateTime likedAt;

  public MicroFeedLike() {}

  public MicroFeedLike(Long userId, MicroFeedItem item, LocalDateTime likedAt) {
    this.userId = userId;
    this.item = item;
    this.likedAt = likedAt;
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

  public MicroFeedItem getItem() {
    return item;
  }

  public void setItem(MicroFeedItem item) {
    this.item = item;
  }

  public LocalDateTime getLikedAt() {
    return likedAt;
  }

  public void setLikedAt(LocalDateTime likedAt) {
    this.likedAt = likedAt;
  }
}
