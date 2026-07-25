package com.smartcampus.platform.doubt.entity;

import java.time.LocalDateTime;

import com.smartcampus.platform.answer.entity.Answer;
import com.smartcampus.platform.assignment.entity.Assignment;
import com.smartcampus.platform.student.entity.Student;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "doubts")
public class Doubt {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @ManyToOne
  @JoinColumn(name = "assignment_id")
  private Assignment assignment;

  @NotBlank
  @Column(nullable = false)
  private String title;

  @Lob
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private DoubtStatus status;

  @ManyToOne
  @JoinColumn(name = "accepted_answer_id")
  private Answer acceptedAnswer;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public Doubt() {}

  public Doubt(Student student, Assignment assignment, String title, String description, DoubtStatus status) {
    this.student = student;
    this.assignment = assignment;
    this.title = title;
    this.description = description;
    this.status = status;
  }

  @PrePersist
  public void onCreate() {
    if (createdAt == null) {
      createdAt = LocalDateTime.now();
    }
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

  public Assignment getAssignment() {
    return assignment;
  }

  public void setAssignment(Assignment assignment) {
    this.assignment = assignment;
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

  public DoubtStatus getStatus() {
    return status;
  }

  public void setStatus(DoubtStatus status) {
    this.status = status;
  }

  public Answer getAcceptedAnswer() {
    return acceptedAnswer;
  }

  public void setAcceptedAnswer(Answer acceptedAnswer) {
    this.acceptedAnswer = acceptedAnswer;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
