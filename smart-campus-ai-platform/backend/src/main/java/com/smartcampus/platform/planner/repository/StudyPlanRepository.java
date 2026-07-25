package com.smartcampus.platform.planner.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.planner.entity.StudyPlan;

public interface StudyPlanRepository extends JpaRepository<StudyPlan, Long> {
  List<StudyPlan> findByUserIdOrderByCreatedAtDesc(Long userId);
  Optional<StudyPlan> findFirstByUserIdOrderByCreatedAtDesc(Long userId);
}
