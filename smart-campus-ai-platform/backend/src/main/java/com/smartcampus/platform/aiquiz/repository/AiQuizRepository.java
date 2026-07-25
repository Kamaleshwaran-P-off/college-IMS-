package com.smartcampus.platform.aiquiz.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.aiquiz.entity.AiQuiz;

public interface AiQuizRepository extends JpaRepository<AiQuiz, Long> {
  Optional<AiQuiz> findTopByClassNameOrderByCreatedAtDesc(String className);
  Optional<AiQuiz> findTopByClassNameAndIsVisibleTrueOrderByCreatedAtDesc(String className);
  List<AiQuiz> findByCreatedByIdOrderByCreatedAtDesc(Long staffId);
}
