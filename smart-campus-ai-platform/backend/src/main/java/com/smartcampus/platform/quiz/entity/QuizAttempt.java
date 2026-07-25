package com.smartcampus.platform.quiz.entity;

import java.time.LocalDateTime;

import com.smartcampus.platform.auth.entity.User;
import jakarta.persistence.*;

@Entity
@Table(name = "quiz_attempts")
public class QuizAttempt {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne(optional = false)
  @JoinColumn(name = "aptitude_question_id", nullable = false)
  private QuizQuestion aptitudeQuestion;

  @ManyToOne(optional = false)
  @JoinColumn(name = "dsa_question_id", nullable = false)
  private QuizQuestion dsaQuestion;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private QuizOption aptitudeAnswer;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private QuizOption dsaAnswer;

  @Column(nullable = false)
  private int correctCount;

  @Column(nullable = false)
  private boolean passed;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public QuizAttempt() {}

  public QuizAttempt(
      User user,
      QuizQuestion aptitudeQuestion,
      QuizQuestion dsaQuestion,
      QuizOption aptitudeAnswer,
      QuizOption dsaAnswer,
      int correctCount,
      boolean passed
  ) {
    this.user = user;
    this.aptitudeQuestion = aptitudeQuestion;
    this.dsaQuestion = dsaQuestion;
    this.aptitudeAnswer = aptitudeAnswer;
    this.dsaAnswer = dsaAnswer;
    this.correctCount = correctCount;
    this.passed = passed;
  }

  @PrePersist
  public void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
  }

  public Long getId() {
    return id;
  }

  public User getUser() {
    return user;
  }

  public QuizQuestion getAptitudeQuestion() {
    return aptitudeQuestion;
  }

  public QuizQuestion getDsaQuestion() {
    return dsaQuestion;
  }

  public QuizOption getAptitudeAnswer() {
    return aptitudeAnswer;
  }

  public QuizOption getDsaAnswer() {
    return dsaAnswer;
  }

  public int getCorrectCount() {
    return correctCount;
  }

  public boolean isPassed() {
    return passed;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
