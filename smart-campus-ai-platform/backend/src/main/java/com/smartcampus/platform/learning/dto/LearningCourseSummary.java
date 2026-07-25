package com.smartcampus.platform.learning.dto;

import java.time.LocalDateTime;

public class LearningCourseSummary {
  private Long id;
  private String title;
  private String description;
  private LocalDateTime createdAt;
  private int topicCount;

  public LearningCourseSummary() {}

  public LearningCourseSummary(Long id, String title, String description, LocalDateTime createdAt, int topicCount) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.createdAt = createdAt;
    this.topicCount = topicCount;
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

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public int getTopicCount() {
    return topicCount;
  }

  public void setTopicCount(int topicCount) {
    this.topicCount = topicCount;
  }
}
