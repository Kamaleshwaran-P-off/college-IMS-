package com.smartcampus.platform.attendance.dto;

import java.time.LocalDate;

import com.smartcampus.platform.attendance.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

public class AttendanceRequest {
  @NotNull
  private Long studentId;

  private Long staffId;

  @NotNull
  private LocalDate classDate;

  @NotNull
  private AttendanceStatus status;

  private String courseCode;

  public Long getStudentId() {
    return studentId;
  }

  public void setStudentId(Long studentId) {
    this.studentId = studentId;
  }

  public Long getStaffId() {
    return staffId;
  }

  public void setStaffId(Long staffId) {
    this.staffId = staffId;
  }

  public LocalDate getClassDate() {
    return classDate;
  }

  public void setClassDate(LocalDate classDate) {
    this.classDate = classDate;
  }

  public AttendanceStatus getStatus() {
    return status;
  }

  public void setStatus(AttendanceStatus status) {
    this.status = status;
  }

  public String getCourseCode() {
    return courseCode;
  }

  public void setCourseCode(String courseCode) {
    this.courseCode = courseCode;
  }
}
