package com.smartcampus.platform.learning.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "learning_courses")
public class LearningCourse {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(length = 1000)
  private String description;

  @Column(nullable = false, name = "faculty_id")
  private Long facultyId;

  private String fileName;

  private String contentType;

  @Lob
  @Column(columnDefinition = "LONGBLOB")
  private byte[] fileData;

  @Lob
  @Column(columnDefinition = "LONGTEXT")
  private String extractedText;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @OneToMany(mappedBy = "course", fetch = FetchType.LAZY, orphanRemoval = true)
  @OrderBy("topicOrder ASC")
  private List<LearningTopic> topics = new ArrayList<>();

  public LearningCourse() {}

  public LearningCourse(
      String title,
      String description,
      Long facultyId,
      String fileName,
      String contentType,
      byte[] fileData,
      String extractedText,
      LocalDateTime createdAt
  ) {
    this.title = title;
    this.description = description;
    this.facultyId = facultyId;
    this.fileName = fileName;
    this.contentType = contentType;
    this.fileData = fileData;
    this.extractedText = extractedText;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Long getFacultyId() {
    return facultyId;
  }

  public void setFacultyId(Long facultyId) {
    this.facultyId = facultyId;
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

  public String getExtractedText() {
    return extractedText;
  }

  public void setExtractedText(String extractedText) {
    this.extractedText = extractedText;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public List<LearningTopic> getTopics() {
    return topics;
  }

  public void setTopics(List<LearningTopic> topics) {
    this.topics = topics;
  }
}
