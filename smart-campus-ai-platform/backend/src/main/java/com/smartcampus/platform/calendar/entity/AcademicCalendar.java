package com.smartcampus.platform.calendar.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "academic_calendar")
public class AcademicCalendar {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String fileName;

  @Column(nullable = false)
  private String contentType;

  @Lob
  @Column(nullable = false, columnDefinition = "LONGBLOB")
  private byte[] fileData;

  @Column(nullable = false)
  private LocalDateTime uploadedAt;

  public AcademicCalendar() {}

  public AcademicCalendar(String fileName, String contentType, byte[] fileData, LocalDateTime uploadedAt) {
    this.fileName = fileName;
    this.contentType = contentType;
    this.fileData = fileData;
    this.uploadedAt = uploadedAt;
  }

  public Long getId() {
    return id;
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

  public LocalDateTime getUploadedAt() {
    return uploadedAt;
  }

  public void setUploadedAt(LocalDateTime uploadedAt) {
    this.uploadedAt = uploadedAt;
  }
}
