package com.smartcampus.platform.marks.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.marks.entity.StudentMarks;

public interface StudentMarksRepository extends JpaRepository<StudentMarks, Long> {
  List<StudentMarks> findByStudentId(Long studentId);
  List<StudentMarks> findByStudentIdAndCourseCodeIgnoreCase(Long studentId, String courseCode);
}
