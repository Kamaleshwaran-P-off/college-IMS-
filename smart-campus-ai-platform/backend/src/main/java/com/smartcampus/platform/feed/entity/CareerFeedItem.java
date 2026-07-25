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
@Table(name = "career_feed_items")
public class CareerFeedItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private String creator;

  @Column(length = 2000)
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CareerCategory category;

  private String sourceUrl;

  private String thumbnailUrl;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  public CareerFeedItem() {}

  public CareerFeedItem(
      String title,
      String creator,
      String description,
      CareerCategory category,
      String sourceUrl,
      String thumbnailUrl,
      LocalDateTime createdAt
  ) {
    this.title = title;
    this.creator = creator;
    this.description = description;
    this.category = category;
    this.sourceUrl = sourceUrl;
    this.thumbnailUrl = thumbnailUrl;
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

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public CareerCategory getCategory() {
    return category;
  }

  public void setCategory(CareerCategory category) {
    this.category = category;
  }

  public String getSourceUrl() {
    return sourceUrl;
  }

  public void setSourceUrl(String sourceUrl) {
    this.sourceUrl = sourceUrl;
  }

  public String getThumbnailUrl() {
    return thumbnailUrl;
  }

  public void setThumbnailUrl(String thumbnailUrl) {
    this.thumbnailUrl = thumbnailUrl;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
