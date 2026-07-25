package com.smartcampus.platform.mentormatching.entity;

import java.time.LocalDateTime;

import com.smartcampus.platform.student.entity.Student;
import jakarta.persistence.*;

@Entity
@Table(name = "student_preferences")
public class StudentPreference {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "student_id", nullable = false, unique = true)
  private Student student;

  @Lob
  private String requiredSkills;

  @Lob
  private String learningGoals;

  @Column(length = 100)
  private String mentorType;

  @Column(length = 255)
  private String availability;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  public StudentPreference() {}

  public StudentPreference(
      Student student,
      String requiredSkills,
      String learningGoals,
      String mentorType,
      String availability,
      LocalDateTime updatedAt
  ) {
    this.student = student;
    this.requiredSkills = requiredSkills;
    this.learningGoals = learningGoals;
    this.mentorType = mentorType;
    this.availability = availability;
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

  public String getRequiredSkills() {
    return requiredSkills;
  }

  public void setRequiredSkills(String requiredSkills) {
    this.requiredSkills = requiredSkills;
  }

  public String getLearningGoals() {
    return learningGoals;
  }

  public void setLearningGoals(String learningGoals) {
    this.learningGoals = learningGoals;
  }

  public String getMentorType() {
    return mentorType;
  }

  public void setMentorType(String mentorType) {
    this.mentorType = mentorType;
  }

  public String getAvailability() {
    return availability;
  }

  public void setAvailability(String availability) {
    this.availability = availability;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
