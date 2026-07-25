package com.smartcampus.platform.learning.dto;

import java.util.List;

public class LearningCourseDetailResponse {
  private Long id;
  private String title;
  private String description;
  private List<LearningTopicResponse> topics;

  public LearningCourseDetailResponse() {}

  public LearningCourseDetailResponse(Long id, String title, String description, List<LearningTopicResponse> topics) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.topics = topics;
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

  public List<LearningTopicResponse> getTopics() {
    return topics;
  }

  public void setTopics(List<LearningTopicResponse> topics) {
    this.topics = topics;
  }
}
