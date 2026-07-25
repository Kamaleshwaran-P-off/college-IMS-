package com.smartcampus.platform.attendance.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartcampus.platform.attendance.entity.Attendance;
import com.smartcampus.platform.attendance.entity.AttendanceStatus;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
  long countByStudentIdAndClassDateBetween(Long studentId, LocalDate start, LocalDate end);
  long countByStudentIdAndClassDateBetweenAndStatus(Long studentId, LocalDate start, LocalDate end, AttendanceStatus status);
  List<Attendance> findByStudentIdAndClassDateBetween(Long studentId, LocalDate start, LocalDate end);
  List<Attendance> findByRecordedByIdAndClassDateBetween(Long staffId, LocalDate start, LocalDate end);
  boolean existsByStudentIdAndClassDateAndCourseCodeIgnoreCase(Long studentId, LocalDate classDate, String courseCode);
  List<Attendance> findByStudentIdAndCourseCodeIgnoreCase(Long studentId, String courseCode);
  List<Attendance> findByStudentIdAndCourseCodeIgnoreCaseAndClassDateBetween(Long studentId, String courseCode, LocalDate start, LocalDate end);
  List<Attendance> findByStudentDepartmentIgnoreCaseAndStudentSectionIgnoreCaseAndCourseCodeIgnoreCase(
      String department,
      String section,
      String courseCode
  );
  List<Attendance> findByStudentDepartmentIgnoreCaseAndStudentSectionIgnoreCaseAndCourseCodeIgnoreCaseAndClassDateBetween(
      String department,
      String section,
      String courseCode,
      LocalDate start,
      LocalDate end
  );
}
