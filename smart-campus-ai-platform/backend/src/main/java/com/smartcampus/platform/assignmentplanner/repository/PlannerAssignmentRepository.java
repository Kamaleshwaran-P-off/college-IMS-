package com.smartcampus.platform.assignmentplanner.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.assignmentplanner.entity.PlannerAssignment;

public interface PlannerAssignmentRepository extends JpaRepository<PlannerAssignment, Long> {
  List<PlannerAssignment> findByTargetStudentId(Long studentId);

  List<PlannerAssignment> findByTargetDepartmentAndTargetSection(String targetDepartment, String targetSection);

  List<PlannerAssignment> findByCreatedById(Long staffId);
}
