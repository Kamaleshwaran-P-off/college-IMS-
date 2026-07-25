package com.smartcampus.platform.learning.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.learning.entity.LearningTopic;

public interface LearningTopicRepository extends JpaRepository<LearningTopic, Long> {
  List<LearningTopic> findByCourseIdOrderByTopicOrderAsc(Long courseId);
}
