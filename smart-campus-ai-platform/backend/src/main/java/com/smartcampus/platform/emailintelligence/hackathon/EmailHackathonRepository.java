package com.smartcampus.platform.emailintelligence.hackathon;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface EmailHackathonRepository extends JpaRepository<EmailHackathon, Long> {
  Optional<EmailHackathon> findByEmailId(Long emailId);

  List<EmailHackathon> findByUserIdOrderByCreatedAtDesc(Long userId);
}
