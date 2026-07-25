package com.smartcampus.platform.coursework.assignment.entity;

import java.time.LocalDateTime;

import com.smartcampus.platform.student.entity.Student;
import jakarta.persistence.*;

@Entity
@Table(name = "assignment_submissions")
public class AssignmentSubmission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "assignment_id", nullable = false)
  private CourseAssignment assignment;

  @ManyToOne(optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @Lob
  private String answerText;

  private String fileName;

  private String contentType;

  @Lob
  @Column(columnDefinition = "LONGBLOB")
  private byte[] fileData;

  private Double marks;

  @Lob
  private String feedback;

  @Column(nullable = false)
  private LocalDateTime submittedAt;

  private LocalDateTime gradedAt;

  public AssignmentSubmission() {}

  public AssignmentSubmission(
      CourseAssignment assignment,
      Student student,
      String answerText,
      String fileName,
      String contentType,
      byte[] fileData,
      LocalDateTime submittedAt
  ) {
    this.assignment = assignment;
    this.student = student;
    this.answerText = answerText;
    this.fileName = fileName;
    this.contentType = contentType;
    this.fileData = fileData;
    this.submittedAt = submittedAt;
  }

  public Long getId() {
    return id;
  }

  public CourseAssignment getAssignment() {
    return assignment;
  }

  public void setAssignment(CourseAssignment assignment) {
    this.assignment = assignment;
  }

  public Student getStudent() {
    return student;
  }

  public void setStudent(Student student) {
    this.student = student;
  }

  public String getAnswerText() {
    return answerText;
  }

  public void setAnswerText(String answerText) {
    this.answerText = answerText;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public byte[] getFileData() {
    return fileData;
  }

  public void setFileData(byte[] fileData) {
    this.fileData = fileData;
  }

  public Double getMarks() {
    return marks;
  }

  public void setMarks(Double marks) {
    this.marks = marks;
  }

  public String getFeedback() {
    return feedback;
  }

  public void setFeedback(String feedback) {
    this.feedback = feedback;
  }

  public LocalDateTime getSubmittedAt() {
    return submittedAt;
  }

  public void setSubmittedAt(LocalDateTime submittedAt) {
    this.submittedAt = submittedAt;
  }

  public LocalDateTime getGradedAt() {
    return gradedAt;
  }

  public void setGradedAt(LocalDateTime gradedAt) {
    this.gradedAt = gradedAt;
  }
}
