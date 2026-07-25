package com.smartcampus.platform.mentormatching.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.mentormatching.entity.FacultySkill;

public interface FacultySkillRepository extends JpaRepository<FacultySkill, Long> {
  Optional<FacultySkill> findByStaffId(Long staffId);
}
