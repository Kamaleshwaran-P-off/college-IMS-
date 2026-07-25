package com.smartcampus.platform.assignment.entity;

import java.time.LocalDate;

import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.student.entity.Student;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "assignments")
public class Assignment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "staff_id", nullable = false)
  private Staff createdBy;

  @ManyToOne(optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private Student assignedStudent;

  @NotBlank
  @Column(nullable = false)
  private String title;

  @Lob
  private String description;

  @NotNull
  @Column(nullable = false)
  private LocalDate dueDate;

  @Column(length = 50)
  private String courseCode;

  public Assignment() {}

  public Assignment(Staff createdBy, Student assignedStudent, String title, String description, LocalDate dueDate, String courseCode) {
    this.createdBy = createdBy;
    this.assignedStudent = assignedStudent;
    this.title = title;
    this.description = description;
    this.dueDate = dueDate;
    this.courseCode = courseCode;
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

  public Student getAssignedStudent() {
    return assignedStudent;
  }

  public void setAssignedStudent(Student assignedStudent) {
    this.assignedStudent = assignedStudent;
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

  public String getCourseCode() {
    return courseCode;
  }

  public void setCourseCode(String courseCode) {
    this.courseCode = courseCode;
  }
}
