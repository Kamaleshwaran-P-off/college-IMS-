package com.smartcampus.platform.learning.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.learning.entity.LearningCourse;

public interface LearningCourseRepository extends JpaRepository<LearningCourse, Long> {
  List<LearningCourse> findByFacultyIdOrderByCreatedAtDesc(Long facultyId);
}
