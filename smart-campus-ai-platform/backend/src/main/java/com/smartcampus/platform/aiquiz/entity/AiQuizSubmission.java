package com.smartcampus.platform.aiquiz.entity;

import java.time.LocalDateTime;

import com.smartcampus.platform.student.entity.Student;
import jakarta.persistence.*;

@Entity
@Table(name = "ai_quiz_submissions")
public class AiQuizSubmission {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "quiz_id", nullable = false)
  private AiQuiz quiz;

  @ManyToOne(optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @Lob
  private String answersJson;

  private Integer score;

  private Integer total;

  private Integer timeTakenSeconds;

  private String certificateFileName;

  private String certificateContentType;

  @Lob
  private byte[] certificateData;

  @Column(nullable = false)
  private LocalDateTime submittedAt;

  public AiQuizSubmission() {}

  public AiQuizSubmission(
      AiQuiz quiz,
      Student student,
      String answersJson,
      Integer score,
      Integer total,
      Integer timeTakenSeconds,
      String certificateFileName,
      String certificateContentType,
      byte[] certificateData,
      LocalDateTime submittedAt
  ) {
    this.quiz = quiz;
    this.student = student;
    this.answersJson = answersJson;
    this.score = score;
    this.total = total;
    this.timeTakenSeconds = timeTakenSeconds;
    this.certificateFileName = certificateFileName;
    this.certificateContentType = certificateContentType;
    this.certificateData = certificateData;
    this.submittedAt = submittedAt;
  }

  public Long getId() {
    return id;
  }

  public AiQuiz getQuiz() {
    return quiz;
  }

  public void setQuiz(AiQuiz quiz) {
    this.quiz = quiz;
  }

  public Student getStudent() {
    return student;
  }

  public void setStudent(Student student) {
    this.student = student;
  }

  public String getAnswersJson() {
    return answersJson;
  }

  public void setAnswersJson(String answersJson) {
    this.answersJson = answersJson;
  }

  public Integer getScore() {
    return score;
  }

  public void setScore(Integer score) {
    this.score = score;
  }

  public Integer getTotal() {
    return total;
  }

  public void setTotal(Integer total) {
    this.total = total;
  }

  public Integer getTimeTakenSeconds() {
    return timeTakenSeconds;
  }

  public void setTimeTakenSeconds(Integer timeTakenSeconds) {
    this.timeTakenSeconds = timeTakenSeconds;
  }

  public String getCertificateFileName() {
    return certificateFileName;
  }

  public void setCertificateFileName(String certificateFileName) {
    this.certificateFileName = certificateFileName;
  }

  public String getCertificateContentType() {
    return certificateContentType;
  }

  public void setCertificateContentType(String certificateContentType) {
    this.certificateContentType = certificateContentType;
  }

  public byte[] getCertificateData() {
    return certificateData;
  }

  public void setCertificateData(byte[] certificateData) {
    this.certificateData = certificateData;
  }

  public LocalDateTime getSubmittedAt() {
    return submittedAt;
  }

  public void setSubmittedAt(LocalDateTime submittedAt) {
    this.submittedAt = submittedAt;
  }
}
