package com.smartcampus.platform.assignment.dto;

import java.time.LocalDate;

public class AssignmentResponse {
  private Long id;
  private Long staffId;
  private Long studentId;
  private String title;
  private String description;
  private LocalDate dueDate;
  private String courseCode;

  public AssignmentResponse(
      Long id,
      Long staffId,
      Long studentId,
      String title,
      String description,
      LocalDate dueDate,
      String courseCode
  ) {
    this.id = id;
    this.staffId = staffId;
    this.studentId = studentId;
    this.title = title;
    this.description = description;
    this.dueDate = dueDate;
    this.courseCode = courseCode;
  }

  public Long getId() {
    return id;
  }

  public Long getStaffId() {
    return staffId;
  }

  public Long getStudentId() {
    return studentId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public String getCourseCode() {
    return courseCode;
  }
}
