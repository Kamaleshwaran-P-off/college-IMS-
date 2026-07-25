package com.smartcampus.platform.mentormatching.dto;

import com.smartcampus.platform.mentormatching.entity.MentorRequestStatus;

public class MentorRequestResponse {
  private Long id;
  private Long studentId;
  private String studentName;
  private Long mentorId;
  private String mentorName;
  private MentorRequestStatus status;
  private String message;
  private String requestedAt;
  private String respondedAt;

  public MentorRequestResponse(
      Long id,
      Long studentId,
      String studentName,
      Long mentorId,
      String mentorName,
      MentorRequestStatus status,
      String message,
      String requestedAt,
      String respondedAt
  ) {
    this.id = id;
    this.studentId = studentId;
    this.studentName = studentName;
    this.mentorId = mentorId;
    this.mentorName = mentorName;
    this.status = status;
    this.message = message;
    this.requestedAt = requestedAt;
    this.respondedAt = respondedAt;
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

  public Long getMentorId() {
    return mentorId;
  }

  public String getMentorName() {
    return mentorName;
  }

  public MentorRequestStatus getStatus() {
    return status;
  }

  public String getMessage() {
    return message;
  }

  public String getRequestedAt() {
    return requestedAt;
  }

  public String getRespondedAt() {
    return respondedAt;
  }
}
