package com.smartcampus.platform.marks.entity;

import java.time.LocalDateTime;

import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.student.entity.Student;
import jakarta.persistence.*;

@Entity
@Table(name = "marks")
public class StudentMarks {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @ManyToOne
  @JoinColumn(name = "staff_id")
  private Staff recordedBy;

  @Column(nullable = false, length = 50)
  private String courseCode;

  private Double cat1;
  private Double cat2;
  private Double cat3;

  private Double assignmentScore;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  public StudentMarks() {}

  public StudentMarks(
      Student student,
      Staff recordedBy,
      String courseCode,
      Double cat1,
      Double cat2,
      Double cat3,
      Double assignmentScore,
      LocalDateTime updatedAt
  ) {
    this.student = student;
    this.recordedBy = recordedBy;
    this.courseCode = courseCode;
    this.cat1 = cat1;
    this.cat2 = cat2;
    this.cat3 = cat3;
    this.assignmentScore = assignmentScore;
    this.updatedAt = updatedAt;
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

  public Staff getRecordedBy() {
    return recordedBy;
  }

  public void setRecordedBy(Staff recordedBy) {
    this.recordedBy = recordedBy;
  }

  public String getCourseCode() {
    return courseCode;
  }

  public void setCourseCode(String courseCode) {
    this.courseCode = courseCode;
  }

  public Double getCat1() {
    return cat1;
  }

  public void setCat1(Double cat1) {
    this.cat1 = cat1;
  }

  public Double getCat2() {
    return cat2;
  }

  public void setCat2(Double cat2) {
    this.cat2 = cat2;
  }

  public Double getCat3() {
    return cat3;
  }

  public void setCat3(Double cat3) {
    this.cat3 = cat3;
  }

  public Double getAssignmentScore() {
    return assignmentScore;
  }

  public void setAssignmentScore(Double assignmentScore) {
    this.assignmentScore = assignmentScore;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
