package com.smartcampus.platform.gmail.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.gmail.entity.GmailToken;

public interface GmailTokenRepository extends JpaRepository<GmailToken, Long> {
  Optional<GmailToken> findByUserId(Long userId);
}
