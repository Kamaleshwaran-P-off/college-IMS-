package com.smartcampus.platform.learning.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "learning_topics")
public class LearningTopic {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "course_id", nullable = false)
  private LearningCourse course;

  @Column(nullable = false)
  private String title;

  @Column(length = 1200)
  private String description;

  @Column(name = "topic_order")
  private int topicOrder;

  @Lob
  @Column(columnDefinition = "LONGTEXT")
  private String content;

  public LearningTopic() {}

  public LearningTopic(LearningCourse course, String title, String description, int topicOrder, String content) {
    this.course = course;
    this.title = title;
    this.description = description;
    this.topicOrder = topicOrder;
    this.content = content;
  }

  public Long getId() {
    return id;
  }

  public LearningCourse getCourse() {
    return course;
  }

  public void setCourse(LearningCourse course) {
    this.course = course;
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

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
