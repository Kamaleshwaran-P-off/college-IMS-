package com.smartcampus.platform.coursework.assignment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.coursework.assignment.entity.AssignmentSubmission;

public interface AssignmentSubmissionRepository extends JpaRepository<AssignmentSubmission, Long> {
  List<AssignmentSubmission> findByAssignmentId(Long assignmentId);
  List<AssignmentSubmission> findByStudentId(Long studentId);
  Optional<AssignmentSubmission> findByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
}
