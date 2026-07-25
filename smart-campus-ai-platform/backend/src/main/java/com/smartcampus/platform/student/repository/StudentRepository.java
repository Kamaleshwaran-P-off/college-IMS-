package com.smartcampus.platform.student.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.student.entity.Student;

public interface StudentRepository extends JpaRepository<Student, Long> {
  boolean existsByUserId(Long userId);
  Optional<Student> findByUserId(Long userId);
  List<Student> findByDepartmentIgnoreCaseAndSectionIgnoreCase(String department, String section);
}
