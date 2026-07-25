package com.smartcampus.platform.coursework.assignment.dto;

import java.time.LocalDateTime;

public class AssignmentSubmissionResponse {
  private Long id;
  private Long assignmentId;
  private Long studentId;
  private String studentName;
  private String studentCode;
  private String answerText;
  private Double marks;
  private String feedback;
  private LocalDateTime submittedAt;
  private LocalDateTime gradedAt;
  private boolean attachmentAvailable;

  public AssignmentSubmissionResponse() {}

  public AssignmentSubmissionResponse(
      Long id,
      Long assignmentId,
      Long studentId,
      String studentName,
      String studentCode,
      String answerText,
      Double marks,
      String feedback,
      LocalDateTime submittedAt,
      LocalDateTime gradedAt,
      boolean attachmentAvailable
  ) {
    this.id = id;
    this.assignmentId = assignmentId;
    this.studentId = studentId;
    this.studentName = studentName;
    this.studentCode = studentCode;
    this.answerText = answerText;
    this.marks = marks;
    this.feedback = feedback;
    this.submittedAt = submittedAt;
    this.gradedAt = gradedAt;
    this.attachmentAvailable = attachmentAvailable;
  }

  public Long getId() {
    return id;
  }

  public Long getAssignmentId() {
    return assignmentId;
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

  public String getAnswerText() {
    return answerText;
  }

  public Double getMarks() {
    return marks;
  }

  public String getFeedback() {
    return feedback;
  }

  public LocalDateTime getSubmittedAt() {
    return submittedAt;
  }

  public LocalDateTime getGradedAt() {
    return gradedAt;
  }

  public boolean isAttachmentAvailable() {
    return attachmentAvailable;
  }
}
