package com.smartcampus.platform.aiquiz.entity;

import java.time.LocalDateTime;

import com.smartcampus.platform.staff.entity.Staff;
import jakarta.persistence.*;

@Entity
@Table(name = "ai_quizzes")
public class AiQuiz {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "staff_id", nullable = false)
  private Staff createdBy;

  @Column(nullable = false)
  private String title;

  @Lob
  private String syllabus;

  @Column(nullable = false)
  private Integer questionCount;

  @Column(length = 200)
  private String questionTypes;

  @Column
  private Integer durationMinutes;

  @Column(length = 100)
  private String department;

  @Column(length = 50)
  private String className;

  @Column(length = 20)
  private String section;

  @Lob
  @Column(nullable = false)
  private String questionsJson;

  private String pdfFileName;

  private String pdfContentType;

  @Lob
  @Column(columnDefinition = "LONGBLOB")
  private byte[] pdfData;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private Boolean isVisible;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  public AiQuiz() {}

  public AiQuiz(
      Staff createdBy,
      String title,
      String syllabus,
      Integer questionCount,
      String questionTypes,
      Integer durationMinutes,
      String department,
      String className,
      String section,
      String questionsJson,
      String pdfFileName,
      String pdfContentType,
      byte[] pdfData,
      LocalDateTime createdAt,
      Boolean isVisible,
      LocalDateTime updatedAt
  ) {
    this.createdBy = createdBy;
    this.title = title;
    this.syllabus = syllabus;
    this.questionCount = questionCount;
    this.questionTypes = questionTypes;
    this.durationMinutes = durationMinutes;
    this.department = department;
    this.className = className;
    this.section = section;
    this.questionsJson = questionsJson;
    this.pdfFileName = pdfFileName;
    this.pdfContentType = pdfContentType;
    this.pdfData = pdfData;
    this.createdAt = createdAt;
    this.isVisible = isVisible;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public Staff getCreatedBy() {
    return createdBy;
  }

  public void setCreatedBy(Staff createdBy) {
    this.createdBy = createdBy;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getSyllabus() {
    return syllabus;
  }

  public void setSyllabus(String syllabus) {
    this.syllabus = syllabus;
  }

  public Integer getQuestionCount() {
    return questionCount;
  }

  public void setQuestionCount(Integer questionCount) {
    this.questionCount = questionCount;
  }

  public String getQuestionTypes() {
    return questionTypes;
  }

  public void setQuestionTypes(String questionTypes) {
    this.questionTypes = questionTypes;
  }

  public Integer getDurationMinutes() {
    return durationMinutes;
  }

  public void setDurationMinutes(Integer durationMinutes) {
    this.durationMinutes = durationMinutes;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public String getSection() {
    return section;
  }

  public void setSection(String section) {
    this.section = section;
  }

  public String getQuestionsJson() {
    return questionsJson;
  }

  public void setQuestionsJson(String questionsJson) {
    this.questionsJson = questionsJson;
  }

  public String getPdfFileName() {
    return pdfFileName;
  }

  public void setPdfFileName(String pdfFileName) {
    this.pdfFileName = pdfFileName;
  }

  public String getPdfContentType() {
    return pdfContentType;
  }

  public void setPdfContentType(String pdfContentType) {
    this.pdfContentType = pdfContentType;
  }

  public byte[] getPdfData() {
    return pdfData;
  }

  public void setPdfData(byte[] pdfData) {
    this.pdfData = pdfData;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public Boolean getIsVisible() {
    return isVisible;
  }

  public void setIsVisible(Boolean isVisible) {
    this.isVisible = isVisible;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
