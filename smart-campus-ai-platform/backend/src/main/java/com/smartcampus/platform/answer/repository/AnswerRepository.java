package com.smartcampus.platform.answer.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.answer.entity.Answer;

public interface AnswerRepository extends JpaRepository<Answer, Long> {
  List<Answer> findByDoubtIdOrderByCreatedAtAsc(Long doubtId);
}
