package com.smartcampus.platform.learning.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "learning_topic_progress",
    uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "topic_id"})
)
public class LearningTopicProgress {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "student_id", nullable = false)
  private Long studentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "topic_id", nullable = false)
  private LearningTopic topic;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TopicStatus status;

  private Integer bestScore;

  private Integer attempts;

  private LocalDateTime updatedAt;

  public LearningTopicProgress() {}

  public LearningTopicProgress(Long studentId, LearningTopic topic, TopicStatus status, Integer bestScore, Integer attempts, LocalDateTime updatedAt) {
    this.studentId = studentId;
    this.topic = topic;
    this.status = status;
    this.bestScore = bestScore;
    this.attempts = attempts;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public Long getStudentId() {
    return studentId;
  }

  public void setStudentId(Long studentId) {
    this.studentId = studentId;
  }

  public LearningTopic getTopic() {
    return topic;
  }

  public void setTopic(LearningTopic topic) {
    this.topic = topic;
  }

  public TopicStatus getStatus() {
    return status;
  }

  public void setStatus(TopicStatus status) {
    this.status = status;
  }

  public Integer getBestScore() {
    return bestScore;
  }

  public void setBestScore(Integer bestScore) {
    this.bestScore = bestScore;
  }

  public Integer getAttempts() {
    return attempts;
  }

  public void setAttempts(Integer attempts) {
    this.attempts = attempts;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
