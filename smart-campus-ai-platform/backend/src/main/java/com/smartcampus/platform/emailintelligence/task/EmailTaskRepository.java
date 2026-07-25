package com.smartcampus.platform.emailintelligence.task;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailTaskRepository extends JpaRepository<EmailTask, Long> {
  Optional<EmailTask> findByEmailId(Long emailId);

  List<EmailTask> findByUserIdOrderByCreatedAtDesc(Long userId);

  Optional<EmailTask> findByIdAndUserId(Long id, Long userId);
}
