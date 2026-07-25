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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "learning_quiz_attempts")
public class LearningQuizAttempt {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "student_id", nullable = false)
  private Long studentId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "topic_id", nullable = false)
  private LearningTopic topic;

  private int score;

  private boolean passed;

  @Lob
  @Column(columnDefinition = "LONGTEXT")
  private String answersJson;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  public LearningQuizAttempt() {}

  public LearningQuizAttempt(Long studentId, LearningTopic topic, int score, boolean passed, String answersJson, LocalDateTime createdAt) {
    this.studentId = studentId;
    this.topic = topic;
    this.score = score;
    this.passed = passed;
    this.answersJson = answersJson;
    this.createdAt = createdAt;
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

  public int getScore() {
    return score;
  }

  public void setScore(int score) {
    this.score = score;
  }

  public boolean isPassed() {
    return passed;
  }

  public void setPassed(boolean passed) {
    this.passed = passed;
  }

  public String getAnswersJson() {
    return answersJson;
  }

  public void setAnswersJson(String answersJson) {
    this.answersJson = answersJson;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
