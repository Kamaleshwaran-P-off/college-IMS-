package com.smartcampus.platform.staff.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.staff.entity.FacultyClassMapping;

public interface FacultyClassMappingRepository extends JpaRepository<FacultyClassMapping, Long> {
  List<FacultyClassMapping> findByStaffId(Long staffId);
  void deleteByStaffId(Long staffId);
  boolean existsByStaffIdAndClassNameIgnoreCase(Long staffId, String className);
  boolean existsByStaffIdAndDepartmentIgnoreCaseAndSectionIgnoreCase(Long staffId, String department, String section);
}
