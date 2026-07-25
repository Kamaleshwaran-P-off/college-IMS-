package com.smartcampus.platform.learning.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.learning.entity.LearningTopicProgress;

public interface LearningTopicProgressRepository extends JpaRepository<LearningTopicProgress, Long> {
  Optional<LearningTopicProgress> findByStudentIdAndTopicId(Long studentId, Long topicId);

  List<LearningTopicProgress> findByStudentIdAndTopicCourseId(Long studentId, Long courseId);

  List<LearningTopicProgress> findByStudentId(Long studentId);
}
