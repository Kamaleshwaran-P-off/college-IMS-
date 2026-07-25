package com.smartcampus.platform.assignmentplanner.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.assignmentplanner.entity.ScheduledTask;

public interface ScheduledTaskRepository extends JpaRepository<ScheduledTask, Long> {
  List<ScheduledTask> findByStudentIdOrderByTaskDateAsc(Long studentId);

  void deleteByStudentId(Long studentId);

  Optional<ScheduledTask> findByIdAndStudentId(Long id, Long studentId);
}
