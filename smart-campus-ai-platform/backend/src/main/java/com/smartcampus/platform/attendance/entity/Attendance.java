package com.smartcampus.platform.attendance.entity;

import java.time.LocalDate;

import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.student.entity.Student;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "attendance")
public class Attendance {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @ManyToOne
  @JoinColumn(name = "staff_id")
  private Staff recordedBy;

  @NotNull
  @Column(nullable = false)
  private LocalDate classDate;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AttendanceStatus status;

  @Column(length = 50)
  private String courseCode;

  public Attendance() {}

  public Attendance(Student student, Staff recordedBy, LocalDate classDate, AttendanceStatus status, String courseCode) {
    this.student = student;
    this.recordedBy = recordedBy;
    this.classDate = classDate;
    this.status = status;
    this.courseCode = courseCode;
  }

  public Long getId() {
    return id;
  }

  public Student getStudent() {
    return student;
  }

  public void setStudent(Student student) {
    this.student = student;
  }

  public Staff getRecordedBy() {
    return recordedBy;
  }

  public void setRecordedBy(Staff recordedBy) {
    this.recordedBy = recordedBy;
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
