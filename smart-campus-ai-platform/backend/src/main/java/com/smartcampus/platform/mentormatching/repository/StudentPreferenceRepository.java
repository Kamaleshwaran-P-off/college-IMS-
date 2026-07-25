package com.smartcampus.platform.mentormatching.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.mentormatching.entity.StudentPreference;

public interface StudentPreferenceRepository extends JpaRepository<StudentPreference, Long> {
  Optional<StudentPreference> findByStudentId(Long studentId);
}
