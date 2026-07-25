package com.smartcampus.platform.learning.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "learning_quizzes")
public class LearningQuiz {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "topic_id", nullable = false)
  private LearningTopic topic;

  @Lob
  @Column(columnDefinition = "LONGTEXT")
  private String questionsJson;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  public LearningQuiz() {}

  public LearningQuiz(LearningTopic topic, String questionsJson, LocalDateTime createdAt) {
    this.topic = topic;
    this.questionsJson = questionsJson;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public LearningTopic getTopic() {
    return topic;
  }

  public void setTopic(LearningTopic topic) {
    this.topic = topic;
  }

  public String getQuestionsJson() {
    return questionsJson;
  }

  public void setQuestionsJson(String questionsJson) {
    this.questionsJson = questionsJson;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
