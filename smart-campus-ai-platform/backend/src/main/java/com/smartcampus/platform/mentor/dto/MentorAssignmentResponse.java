package com.smartcampus.platform.mentor.dto;

import java.time.LocalDateTime;

public class MentorAssignmentResponse {
  private Long id;
  private Long studentId;
  private String studentName;
  private String studentCode;
  private Long mentorId;
  private String mentorName;
  private String mentorCode;
  private String mentorDepartment;
  private LocalDateTime assignedAt;

  public MentorAssignmentResponse(
      Long id,
      Long studentId,
      String studentName,
      String studentCode,
      Long mentorId,
      String mentorName,
      String mentorCode,
      String mentorDepartment,
      LocalDateTime assignedAt
  ) {
    this.id = id;
    this.studentId = studentId;
    this.studentName = studentName;
    this.studentCode = studentCode;
    this.mentorId = mentorId;
    this.mentorName = mentorName;
    this.mentorCode = mentorCode;
    this.mentorDepartment = mentorDepartment;
    this.assignedAt = assignedAt;
  }

  public Long getId() {
    return id;
  }

  public Long getStudentId() {
    return studentId;
  }

  public String getStudentName() {
    return studentName;
  }

  public String getStudentCode() {
    return studentCode;
  }

  public Long getMentorId() {
    return mentorId;
  }

  public String getMentorName() {
    return mentorName;
  }

  public String getMentorCode() {
    return mentorCode;
  }

  public String getMentorDepartment() {
    return mentorDepartment;
  }

  public LocalDateTime getAssignedAt() {
    return assignedAt;
  }
}
