package com.smartcampus.platform.quiz.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.quiz.entity.QuizCategory;
import com.smartcampus.platform.quiz.entity.QuizQuestion;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
  List<QuizQuestion> findByCategory(QuizCategory category);
}
