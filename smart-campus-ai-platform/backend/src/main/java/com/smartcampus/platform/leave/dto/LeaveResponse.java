package com.smartcampus.platform.leave.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.smartcampus.platform.leave.entity.LeaveStatus;
import com.smartcampus.platform.leave.entity.LeaveType;

public class LeaveResponse {
  private Long id;
  private Long studentId;
  private String studentName;
  private String studentCode;
  private Long mentorId;
  private String mentorName;
  private LeaveType type;
  private LocalDate startDate;
  private LocalDate endDate;
  private String reason;
  private LeaveStatus status;
  private LocalDateTime createdAt;
  private Long decidedById;
  private String decidedByName;
  private LocalDateTime decidedAt;
  private String decisionNote;
  private String adminRemarks;
  private boolean proofAvailable;
  private boolean letterAvailable;

  public LeaveResponse(
      Long id,
      Long studentId,
      String studentName,
      String studentCode,
      Long mentorId,
      String mentorName,
      LeaveType type,
      LocalDate startDate,
      LocalDate endDate,
      String reason,
      LeaveStatus status,
      LocalDateTime createdAt,
      Long decidedById,
      String decidedByName,
      LocalDateTime decidedAt,
      String decisionNote,
      String adminRemarks,
      boolean proofAvailable,
      boolean letterAvailable
  ) {
    this.id = id;
    this.studentId = studentId;
    this.studentName = studentName;
    this.studentCode = studentCode;
    this.mentorId = mentorId;
    this.mentorName = mentorName;
    this.type = type;
    this.startDate = startDate;
    this.endDate = endDate;
    this.reason = reason;
    this.status = status;
    this.createdAt = createdAt;
    this.decidedById = decidedById;
    this.decidedByName = decidedByName;
    this.decidedAt = decidedAt;
    this.decisionNote = decisionNote;
    this.adminRemarks = adminRemarks;
    this.proofAvailable = proofAvailable;
    this.letterAvailable = letterAvailable;
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

  public LeaveType getType() {
    return type;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public String getReason() {
    return reason;
  }

  public LeaveStatus getStatus() {
    return status;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public Long getDecidedById() {
    return decidedById;
  }

  public String getDecidedByName() {
    return decidedByName;
  }

  public LocalDateTime getDecidedAt() {
    return decidedAt;
  }

  public String getDecisionNote() {
    return decisionNote;
  }

  public String getAdminRemarks() {
    return adminRemarks;
  }

  public boolean isProofAvailable() {
    return proofAvailable;
  }

  public boolean isLetterAvailable() {
    return letterAvailable;
  }
}
