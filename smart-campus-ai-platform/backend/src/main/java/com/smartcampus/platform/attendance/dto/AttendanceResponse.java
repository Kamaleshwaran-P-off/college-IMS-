package com.smartcampus.platform.attendance.dto;

import java.time.LocalDate;

import com.smartcampus.platform.attendance.entity.AttendanceStatus;

public class AttendanceResponse {
  private Long id;
  private Long studentId;
  private Long staffId;
  private LocalDate classDate;
  private AttendanceStatus status;
  private String courseCode;

  public AttendanceResponse(
      Long id,
      Long studentId,
      Long staffId,
      LocalDate classDate,
      AttendanceStatus status,
      String courseCode
  ) {
    this.id = id;
    this.studentId = studentId;
    this.staffId = staffId;
    this.classDate = classDate;
    this.status = status;
    this.courseCode = courseCode;
  }

  public Long getId() {
    return id;
  }

  public Long getStudentId() {
    return studentId;
  }

  public Long getStaffId() {
    return staffId;
  }

  public LocalDate getClassDate() {
    return classDate;
  }

  public AttendanceStatus getStatus() {
    return status;
  }

  public String getCourseCode() {
    return courseCode;
  }
}
