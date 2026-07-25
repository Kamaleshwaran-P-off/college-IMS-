package com.smartcampus.platform.coursework.assignment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.coursework.assignment.entity.CourseAssignment;

public interface CourseAssignmentRepository extends JpaRepository<CourseAssignment, Long> {
  List<CourseAssignment> findByClassNameOrderByCreatedAtDesc(String className);
  List<CourseAssignment> findByClassNameAndIsVisibleTrueOrderByCreatedAtDesc(String className);
  List<CourseAssignment> findByClassNameInOrderByCreatedAtDesc(List<String> classNames);
  List<CourseAssignment> findByCreatedByIdOrderByCreatedAtDesc(Long staffId);
  List<CourseAssignment> findByCreatedByIdAndClassNameInOrderByCreatedAtDesc(Long staffId, List<String> classNames);
}
