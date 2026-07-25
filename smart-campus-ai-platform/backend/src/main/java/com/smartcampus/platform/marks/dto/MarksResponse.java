package com.smartcampus.platform.marks.dto;

import java.time.LocalDateTime;

public class MarksResponse {
  private Long id;
  private Long studentId;
  private String studentName;
  private String studentCode;
  private String subject;
  private Double cat1;
  private Double cat2;
  private Double cat3;
  private Double assignmentScore;
  private LocalDateTime updatedAt;

  public MarksResponse(
      Long id,
      Long studentId,
      String studentName,
      String studentCode,
      String subject,
      Double cat1,
      Double cat2,
      Double cat3,
      Double assignmentScore,
      LocalDateTime updatedAt
  ) {
    this.id = id;
    this.studentId = studentId;
    this.studentName = studentName;
    this.studentCode = studentCode;
    this.subject = subject;
    this.cat1 = cat1;
    this.cat2 = cat2;
    this.cat3 = cat3;
    this.assignmentScore = assignmentScore;
    this.updatedAt = updatedAt;
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

  public String getSubject() {
    return subject;
  }

  public Double getCat1() {
    return cat1;
  }

  public Double getCat2() {
    return cat2;
  }

  public Double getCat3() {
    return cat3;
  }

  public Double getAssignmentScore() {
    return assignmentScore;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }
}
