package com.smartcampus.platform.learning.dto;

public class LearningTopicResponse {
  private Long id;
  private String title;
  private String description;
  private int topicOrder;
  private String status;
  private Integer bestScore;

  public LearningTopicResponse() {}

  public LearningTopicResponse(Long id, String title, String description, int topicOrder, String status, Integer bestScore) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.topicOrder = topicOrder;
    this.status = status;
    this.bestScore = bestScore;
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

  public int getTopicOrder() {
    return topicOrder;
  }

  public void setTopicOrder(int topicOrder) {
    this.topicOrder = topicOrder;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public Integer getBestScore() {
    return bestScore;
  }

  public void setBestScore(Integer bestScore) {
    this.bestScore = bestScore;
  }
}
