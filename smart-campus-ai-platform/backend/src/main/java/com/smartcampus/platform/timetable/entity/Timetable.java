package com.smartcampus.platform.timetable.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "timetable")
public class Timetable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String department;

  @Column(nullable = false, length = 20)
  private String section;

  @Column(nullable = false)
  private String fileName;

  @Column(nullable = false)
  private String contentType;

  @Lob
  @Column(nullable = false, columnDefinition = "LONGBLOB")
  private byte[] fileData;

  @Column(nullable = false)
  private LocalDateTime uploadedAt;

  public Timetable() {}

  public Timetable(
      String department,
      String section,
      String fileName,
      String contentType,
      byte[] fileData,
      LocalDateTime uploadedAt
  ) {
    this.department = department;
    this.section = section;
    this.fileName = fileName;
    this.contentType = contentType;
    this.fileData = fileData;
    this.uploadedAt = uploadedAt;
  }

  public Long getId() {
    return id;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public String getSection() {
    return section;
  }

  public void setSection(String section) {
    this.section = section;
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
