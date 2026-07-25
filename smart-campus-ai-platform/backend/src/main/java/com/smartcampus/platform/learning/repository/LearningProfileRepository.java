package com.smartcampus.platform.learning.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.learning.entity.LearningProfile;

public interface LearningProfileRepository extends JpaRepository<LearningProfile, Long> {
  Optional<LearningProfile> findByStudentId(Long studentId);
}
