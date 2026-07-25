package com.smartcampus.platform.aiquiz.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.aiquiz.entity.AiQuizSubmission;

public interface AiQuizSubmissionRepository extends JpaRepository<AiQuizSubmission, Long> {
  Optional<AiQuizSubmission> findByQuizIdAndStudentId(Long quizId, Long studentId);
  List<AiQuizSubmission> findByStudentId(Long studentId);
}
