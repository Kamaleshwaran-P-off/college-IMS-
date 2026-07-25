package com.smartcampus.platform.mentormatching.entity;

import java.time.LocalDateTime;

import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.student.entity.Student;
import jakarta.persistence.*;

@Entity
@Table(name = "mentor_matches")
public class MentorMatch {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @ManyToOne(optional = false)
  @JoinColumn(name = "mentor_id", nullable = false)
  private Staff mentor;

  @Column(nullable = false)
  private Double score;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  public MentorMatch() {}

  public MentorMatch(Student student, Staff mentor, Double score, LocalDateTime createdAt) {
    this.student = student;
    this.mentor = mentor;
    this.score = score;
    this.createdAt = createdAt;
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

  public Staff getMentor() {
    return mentor;
  }

  public void setMentor(Staff mentor) {
    this.mentor = mentor;
  }

  public Double getScore() {
    return score;
  }

  public void setScore(Double score) {
    this.score = score;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
