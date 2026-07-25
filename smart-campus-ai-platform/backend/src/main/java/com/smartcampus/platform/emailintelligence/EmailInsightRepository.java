package com.smartcampus.platform.emailintelligence;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailInsightRepository extends JpaRepository<EmailInsight, Long> {
  Optional<EmailInsight> findByEmailId(Long emailId);

  Page<EmailInsight> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
