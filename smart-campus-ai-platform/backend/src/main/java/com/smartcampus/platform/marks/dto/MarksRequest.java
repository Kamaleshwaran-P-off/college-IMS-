package com.smartcampus.platform.marks.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MarksRequest {
  @NotNull
  private Long studentId;

  @NotBlank
  private String subject;

  private Double cat1;
  private Double cat2;
  private Double cat3;
  private Double assignmentScore;

  public Long getStudentId() {
    return studentId;
  }

  public void setStudentId(Long studentId) {
    this.studentId = studentId;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public Double getCat1() {
    return cat1;
  }

  public void setCat1(Double cat1) {
    this.cat1 = cat1;
  }

  public Double getCat2() {
    return cat2;
  }

  public void setCat2(Double cat2) {
    this.cat2 = cat2;
  }

  public Double getCat3() {
    return cat3;
  }

  public void setCat3(Double cat3) {
    this.cat3 = cat3;
  }

  public Double getAssignmentScore() {
    return assignmentScore;
  }

  public void setAssignmentScore(Double assignmentScore) {
    this.assignmentScore = assignmentScore;
  }
}
