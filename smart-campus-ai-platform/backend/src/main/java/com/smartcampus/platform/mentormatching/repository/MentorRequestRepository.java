package com.smartcampus.platform.mentormatching.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.mentormatching.entity.MentorRequest;
import com.smartcampus.platform.mentormatching.entity.MentorRequestStatus;

public interface MentorRequestRepository extends JpaRepository<MentorRequest, Long> {
  List<MentorRequest> findByMentorIdOrderByRequestedAtDesc(Long mentorId);
  List<MentorRequest> findByStudentIdOrderByRequestedAtDesc(Long studentId);
  List<MentorRequest> findByMentorIdAndStatusOrderByRequestedAtDesc(Long mentorId, MentorRequestStatus status);
}
