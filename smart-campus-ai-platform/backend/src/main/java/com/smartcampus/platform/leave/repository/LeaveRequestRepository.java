package com.smartcampus.platform.leave.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.leave.entity.LeaveRequest;
import com.smartcampus.platform.leave.entity.LeaveStatus;

public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
  List<LeaveRequest> findByStudentIdOrderByCreatedAtDesc(Long studentId);
  List<LeaveRequest> findByMentorIdOrderByCreatedAtDesc(Long mentorId);
  List<LeaveRequest> findByStatusOrderByCreatedAtDesc(LeaveStatus status);
  List<LeaveRequest> findByMentorIdAndStatusOrderByCreatedAtDesc(Long mentorId, LeaveStatus status);
  List<LeaveRequest> findAllByOrderByCreatedAtDesc();
}
