package com.smartcampus.platform.chat.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartcampus.platform.chat.entity.ChatBonus;

public interface ChatBonusRepository extends JpaRepository<ChatBonus, Long> {
  @Query("select coalesce(sum(b.bonusQueries), 0) from ChatBonus b where b.user.id = :userId and b.createdAt >= :start and b.createdAt < :end")
  int sumBonusQueries(@Param("userId") Long userId, @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
