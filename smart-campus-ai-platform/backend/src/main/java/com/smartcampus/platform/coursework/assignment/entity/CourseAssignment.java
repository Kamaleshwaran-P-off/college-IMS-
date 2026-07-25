package com.smartcampus.platform.coursework.assignment.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.smartcampus.platform.staff.entity.Staff;
import jakarta.persistence.*;

@Entity
@Table(name = "course_assignments")
public class CourseAssignment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "staff_id", nullable = false)
  private Staff createdBy;

  @Column(nullable = false)
  private String title;

  @Lob
  private String description;

  private LocalDate dueDate;

  @Column(length = 100)
  private String department;

  @Column(length = 50, nullable = false)
  private String className;

  private String fileName;

  private String contentType;

  @Lob
  @Column(columnDefinition = "LONGBLOB")
  private byte[] fileData;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Column(nullable = false)
  private Boolean isVisible;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  public CourseAssignment() {}

  public CourseAssignment(
      Staff createdBy,
      String title,
      String description,
      LocalDate dueDate,
      String department,
      String className,
      String fileName,
      String contentType,
      byte[] fileData,
      LocalDateTime createdAt,
      Boolean isVisible,
      LocalDateTime updatedAt
  ) {
    this.createdBy = createdBy;
    this.title = title;
    this.description = description;
    this.dueDate = dueDate;
    this.department = department;
    this.className = className;
    this.fileName = fileName;
    this.contentType = contentType;
    this.fileData = fileData;
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

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public LocalDate getDueDate() {
    return dueDate;
  }

  public void setDueDate(LocalDate dueDate) {
    this.dueDate = dueDate;
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
