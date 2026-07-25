package com.smartcampus.platform.gmail.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.gmail.entity.Email;

public interface EmailRepository extends JpaRepository<Email, Long> {
  Optional<Email> findByUserIdAndMessageId(Long userId, String messageId);

  Page<Email> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
