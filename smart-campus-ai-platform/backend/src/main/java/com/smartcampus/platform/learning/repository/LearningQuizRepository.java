package com.smartcampus.platform.learning.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.learning.entity.LearningQuiz;

public interface LearningQuizRepository extends JpaRepository<LearningQuiz, Long> {
  Optional<LearningQuiz> findByTopicId(Long topicId);
}
