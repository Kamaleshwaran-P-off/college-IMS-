package com.smartcampus.platform.planner.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartcampus.platform.planner.entity.StudyTask;

public interface StudyTaskRepository extends JpaRepository<StudyTask, Long> {
  List<StudyTask> findByPlanIdOrderByDayOrderAsc(Long planId);
  long countByPlanId(Long planId);
  long countByPlanIdAndCompletedTrue(Long planId);
  List<StudyTask> findByReminderAtNotNullAndReminderAtLessThanEqualAndReminderSentFalseAndCompletedFalse(LocalDateTime now);

  @Query("""
      select t from StudyTask t
      where t.plan.user.id = :userId
        and t.completed = true
        and t.completedAt is not null
      order by t.completedAt desc
      """)
  List<StudyTask> findCompletedByUser(@Param("userId") Long userId);
}
