package com.smartcampus.platform.doubt.dto;

import java.time.LocalDateTime;

import com.smartcampus.platform.doubt.entity.DoubtStatus;

public class DoubtResponse {
  private Long id;
  private Long studentId;
  private Long studentUserId;
  private String title;
  private String description;
  private DoubtStatus status;
  private LocalDateTime createdAt;
  private Long acceptedAnswerId;

  public DoubtResponse(
      Long id,
      Long studentId,
      Long studentUserId,
      String title,
      String description,
      DoubtStatus status,
      LocalDateTime createdAt,
      Long acceptedAnswerId
  ) {
    this.id = id;
    this.studentId = studentId;
    this.studentUserId = studentUserId;
    this.title = title;
    this.description = description;
    this.status = status;
    this.createdAt = createdAt;
    this.acceptedAnswerId = acceptedAnswerId;
  }

  public Long getId() {
    return id;
  }

  public Long getStudentId() {
    return studentId;
  }

  public Long getStudentUserId() {
    return studentUserId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public DoubtStatus getStatus() {
    return status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public Long getAcceptedAnswerId() {
    return acceptedAnswerId;
  }
}
