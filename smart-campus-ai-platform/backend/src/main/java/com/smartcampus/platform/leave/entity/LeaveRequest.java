package com.smartcampus.platform.leave.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.student.entity.Student;
import jakarta.persistence.*;

@Entity
@Table(name = "leave_requests")
public class LeaveRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @ManyToOne
  @JoinColumn(name = "mentor_id")
  private Staff mentor;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private LeaveType type;

  @Column(nullable = false)
  private LocalDate startDate;

  private LocalDate endDate;

  @Column(length = 600)
  private String reason;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private LeaveStatus status;

  @ManyToOne
  @JoinColumn(name = "decided_by")
  private User decidedBy;

  private LocalDateTime decidedAt;

  @Column(length = 600)
  private String decisionNote;

  @Column(length = 600)
  private String adminRemarks;

  private String proofFileName;

  private String proofContentType;

  @Lob
  private byte[] proofFileData;

  private String letterFileName;

  private String letterContentType;

  @Lob
  private byte[] letterFileData;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public LeaveRequest() {}

  public LeaveRequest(
      Student student,
      Staff mentor,
      LeaveType type,
      LocalDate startDate,
      LocalDate endDate,
      String reason,
      LeaveStatus status
  ) {
    this.student = student;
    this.mentor = mentor;
    this.type = type;
    this.startDate = startDate;
    this.endDate = endDate;
    this.reason = reason;
    this.status = status;
  }

  @PrePersist
  public void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
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

  public Staff getMentor() {
    return mentor;
  }

  public void setMentor(Staff mentor) {
    this.mentor = mentor;
  }

  public LeaveType getType() {
    return type;
  }

  public void setType(LeaveType type) {
    this.type = type;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public LeaveStatus getStatus() {
    return status;
  }

  public void setStatus(LeaveStatus status) {
    this.status = status;
  }

  public User getDecidedBy() {
    return decidedBy;
  }

  public void setDecidedBy(User decidedBy) {
    this.decidedBy = decidedBy;
  }

  public LocalDateTime getDecidedAt() {
    return decidedAt;
  }

  public void setDecidedAt(LocalDateTime decidedAt) {
    this.decidedAt = decidedAt;
  }

  public String getDecisionNote() {
    return decisionNote;
  }

  public void setDecisionNote(String decisionNote) {
    this.decisionNote = decisionNote;
  }

  public String getAdminRemarks() {
    return adminRemarks;
  }

  public void setAdminRemarks(String adminRemarks) {
    this.adminRemarks = adminRemarks;
  }

  public String getProofFileName() {
    return proofFileName;
  }

  public void setProofFileName(String proofFileName) {
    this.proofFileName = proofFileName;
  }

  public String getProofContentType() {
    return proofContentType;
  }

  public void setProofContentType(String proofContentType) {
    this.proofContentType = proofContentType;
  }

  public byte[] getProofFileData() {
    return proofFileData;
  }

  public void setProofFileData(byte[] proofFileData) {
    this.proofFileData = proofFileData;
  }

  public String getLetterFileName() {
    return letterFileName;
  }

  public void setLetterFileName(String letterFileName) {
    this.letterFileName = letterFileName;
  }

  public String getLetterContentType() {
    return letterContentType;
  }

  public void setLetterContentType(String letterContentType) {
    this.letterContentType = letterContentType;
  }

  public byte[] getLetterFileData() {
    return letterFileData;
  }

  public void setLetterFileData(byte[] letterFileData) {
    this.letterFileData = letterFileData;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
