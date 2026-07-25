package com.smartcampus.platform.mentormatching.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.mentormatching.entity.MentorMatch;

public interface MentorMatchRepository extends JpaRepository<MentorMatch, Long> {
  List<MentorMatch> findByStudentIdOrderByScoreDesc(Long studentId);
  List<MentorMatch> findByMentorId(Long mentorId);
  void deleteByStudentId(Long studentId);
}
