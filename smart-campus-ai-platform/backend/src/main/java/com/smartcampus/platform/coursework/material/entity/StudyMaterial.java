package com.smartcampus.platform.coursework.material.entity;

import java.time.LocalDateTime;

import com.smartcampus.platform.staff.entity.Staff;
import jakarta.persistence.*;

@Entity
@Table(name = "study_materials")
public class StudyMaterial {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "staff_id", nullable = false)
  private Staff uploadedBy;

  @Column(nullable = false)
  private String title;

  @Lob
  private String description;

  @Column(length = 100)
  private String department;

  @Column(length = 50)
  private String className;

  private String fileName;

  private String contentType;

  @Lob
  @Column(columnDefinition = "LONGBLOB")
  private byte[] fileData;

  @Column(nullable = false)
  private LocalDateTime uploadedAt;

  @Column(nullable = false)
  private Boolean isVisible;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  public StudyMaterial() {}

  public StudyMaterial(
      Staff uploadedBy,
      String title,
      String description,
      String department,
      String className,
      String fileName,
      String contentType,
      byte[] fileData,
      LocalDateTime uploadedAt,
      Boolean isVisible,
      LocalDateTime updatedAt
  ) {
    this.uploadedBy = uploadedBy;
    this.title = title;
    this.description = description;
    this.department = department;
    this.className = className;
    this.fileName = fileName;
    this.contentType = contentType;
    this.fileData = fileData;
    this.uploadedAt = uploadedAt;
    this.isVisible = isVisible;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public Staff getUploadedBy() {
    return uploadedBy;
  }

  public void setUploadedBy(Staff uploadedBy) {
    this.uploadedBy = uploadedBy;
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
