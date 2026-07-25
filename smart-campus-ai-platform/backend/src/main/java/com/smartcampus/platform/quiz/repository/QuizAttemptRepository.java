package com.smartcampus.platform.quiz.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.quiz.entity.QuizAttempt;

public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {
  boolean existsByUserIdAndPassedTrueAndCreatedAtBetween(Long userId, LocalDateTime start, LocalDateTime end);
}
