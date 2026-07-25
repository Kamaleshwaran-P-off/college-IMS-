package com.smartcampus.platform.staff.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.staff.entity.FacultySubjectMapping;

public interface FacultySubjectMappingRepository extends JpaRepository<FacultySubjectMapping, Long> {
  List<FacultySubjectMapping> findByStaffId(Long staffId);

  Optional<FacultySubjectMapping> findByStaffIdAndSubjectIgnoreCaseAndDepartmentIgnoreCaseAndSectionIgnoreCase(
      Long staffId,
      String subject,
      String department,
      String section
  );

  boolean existsByStaffId(Long staffId);
}
