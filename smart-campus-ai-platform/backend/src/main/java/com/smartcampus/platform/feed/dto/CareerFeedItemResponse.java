package com.smartcampus.platform.feed.dto;

import java.time.LocalDateTime;

public class CareerFeedItemResponse {
  private Long id;
  private String title;
  private String creator;
  private String description;
  private String category;
  private String sourceUrl;
  private String thumbnailUrl;
  private LocalDateTime createdAt;
  private boolean saved;

  public CareerFeedItemResponse() {}

  public CareerFeedItemResponse(
      Long id,
      String title,
      String creator,
      String description,
      String category,
      String sourceUrl,
      String thumbnailUrl,
      LocalDateTime createdAt,
      boolean saved
  ) {
    this.id = id;
    this.title = title;
    this.creator = creator;
    this.description = description;
    this.category = category;
    this.sourceUrl = sourceUrl;
    this.thumbnailUrl = thumbnailUrl;
    this.createdAt = createdAt;
    this.saved = saved;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
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

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
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

  public boolean isSaved() {
    return saved;
  }

  public void setSaved(boolean saved) {
    this.saved = saved;
  }
}
