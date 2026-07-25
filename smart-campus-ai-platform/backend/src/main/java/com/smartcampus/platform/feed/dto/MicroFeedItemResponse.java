package com.smartcampus.platform.feed.dto;

import java.time.LocalDateTime;

public class MicroFeedItemResponse {
  private Long id;
  private String title;
  private String description;
  private String type;
  private String videoUrl;
  private LocalDateTime createdAt;
  private boolean saved;
  private boolean liked;

  public MicroFeedItemResponse() {}

  public MicroFeedItemResponse(
      Long id,
      String title,
      String description,
      String type,
      String videoUrl,
      LocalDateTime createdAt,
      boolean saved,
      boolean liked
  ) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.type = type;
    this.videoUrl = videoUrl;
    this.createdAt = createdAt;
    this.saved = saved;
    this.liked = liked;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
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

  public boolean isSaved() {
    return saved;
  }

  public void setSaved(boolean saved) {
    this.saved = saved;
  }

  public boolean isLiked() {
    return liked;
  }

  public void setLiked(boolean liked) {
    this.liked = liked;
  }
}
