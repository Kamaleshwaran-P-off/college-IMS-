package com.smartcampus.platform.feed.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "micro_feed_items")
public class MicroFeedItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(length = 2000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private MicroFeedType type;

  private String videoUrl;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  public MicroFeedItem() {}

  public MicroFeedItem(String title, String description, MicroFeedType type, String videoUrl, LocalDateTime createdAt) {
    this.title = title;
    this.description = description;
    this.type = type;
    this.videoUrl = videoUrl;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public MicroFeedType getType() {
    return type;
  }

  public void setType(MicroFeedType type) {
    this.type = type;
  }

  public String getVideoUrl() {
    return videoUrl;
  }

  public void setVideoUrl(String videoUrl) {
    this.videoUrl = videoUrl;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
