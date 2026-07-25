package com.smartcampus.platform.mentor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.mentor.entity.MentorAssignment;

public interface MentorAssignmentRepository extends JpaRepository<MentorAssignment, Long> {
  Optional<MentorAssignment> findByStudentId(Long studentId);
  List<MentorAssignment> findByMentorId(Long mentorId);
  long countByMentorId(Long mentorId);
}
