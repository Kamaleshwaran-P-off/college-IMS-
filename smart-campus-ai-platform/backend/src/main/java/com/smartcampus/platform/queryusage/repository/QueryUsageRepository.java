package com.smartcampus.platform.queryusage.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.queryusage.entity.QueryUsage;

public interface QueryUsageRepository extends JpaRepository<QueryUsage, Long> {
  long countByUserIdAndQueryTypeAndCreatedAtBetween(
      Long userId,
      String queryType,
      LocalDateTime start,
      LocalDateTime end
  );
}
