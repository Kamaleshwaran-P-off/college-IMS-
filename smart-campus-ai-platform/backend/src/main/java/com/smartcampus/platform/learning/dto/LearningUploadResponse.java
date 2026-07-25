package com.smartcampus.platform.learning.dto;

public class LearningUploadResponse {
  private Long courseId;
  private int topicCount;

  public LearningUploadResponse() {}

  public LearningUploadResponse(Long courseId, int topicCount) {
    this.courseId = courseId;
    this.topicCount = topicCount;
  }

  public Long getCourseId() {
    return courseId;
  }

  public void setCourseId(Long courseId) {
    this.courseId = courseId;
  }

  public int getTopicCount() {
    return topicCount;
  }

  public void setTopicCount(int topicCount) {
    this.topicCount = topicCount;
  }
}
