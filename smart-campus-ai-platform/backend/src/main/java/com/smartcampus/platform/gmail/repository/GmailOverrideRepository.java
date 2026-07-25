package com.smartcampus.platform.gmail.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.gmail.entity.GmailOverride;

public interface GmailOverrideRepository extends JpaRepository<GmailOverride, Long> {
  List<GmailOverride> findByUserId(Long userId);

  Optional<GmailOverride> findByUserIdAndMessageId(Long userId, String messageId);
}
