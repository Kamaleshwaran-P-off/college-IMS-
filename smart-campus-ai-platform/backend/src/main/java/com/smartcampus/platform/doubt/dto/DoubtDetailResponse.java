package com.smartcampus.platform.doubt.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.smartcampus.platform.doubt.entity.DoubtStatus;

public class DoubtDetailResponse {
  private Long id;
  private Long studentId;
  private Long studentUserId;
  private String studentName;
  private String title;
  private String description;
  private DoubtStatus status;
  private LocalDateTime createdAt;
  private Long acceptedAnswerId;
  private List<DoubtAnswerResponse> answers;

  public DoubtDetailResponse(
      Long id,
      Long studentId,
      Long studentUserId,
      String studentName,
      String title,
      String description,
      DoubtStatus status,
      LocalDateTime createdAt,
      Long acceptedAnswerId,
      List<DoubtAnswerResponse> answers
  ) {
    this.id = id;
    this.studentId = studentId;
    this.studentUserId = studentUserId;
    this.studentName = studentName;
    this.title = title;
    this.description = description;
    this.status = status;
    this.createdAt = createdAt;
    this.acceptedAnswerId = acceptedAnswerId;
    this.answers = answers;
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

  public String getStudentName() {
    return studentName;
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

  public List<DoubtAnswerResponse> getAnswers() {
    return answers;
  }
}
