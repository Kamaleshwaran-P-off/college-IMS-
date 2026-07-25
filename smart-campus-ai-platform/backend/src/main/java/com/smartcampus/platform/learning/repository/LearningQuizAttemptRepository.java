package com.smartcampus.platform.learning.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.learning.entity.LearningQuizAttempt;

public interface LearningQuizAttemptRepository extends JpaRepository<LearningQuizAttempt, Long> {
  List<LearningQuizAttempt> findByStudentIdAndTopicId(Long studentId, Long topicId);
}
