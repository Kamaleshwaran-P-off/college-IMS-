package com.smartcampus.platform.mentormatching.entity;

import java.time.LocalDateTime;

import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.student.entity.Student;
import jakarta.persistence.*;

@Entity
@Table(name = "mentor_requests")
public class MentorRequest {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @ManyToOne(optional = false)
  @JoinColumn(name = "mentor_id", nullable = false)
  private Staff mentor;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private MentorRequestStatus status;

  @Lob
  private String message;

  @Column(nullable = false)
  private LocalDateTime requestedAt;

  private LocalDateTime respondedAt;

  public MentorRequest() {}

  public MentorRequest(
      Student student,
      Staff mentor,
      MentorRequestStatus status,
      String message,
      LocalDateTime requestedAt
  ) {
    this.student = student;
    this.mentor = mentor;
    this.status = status;
    this.message = message;
    this.requestedAt = requestedAt;
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

  public MentorRequestStatus getStatus() {
    return status;
  }

  public void setStatus(MentorRequestStatus status) {
    this.status = status;
  }

  public String getMessage() {
    return message;
  }

  public void setMessage(String message) {
    this.message = message;
  }

  public LocalDateTime getRequestedAt() {
    return requestedAt;
  }

  public void setRequestedAt(LocalDateTime requestedAt) {
    this.requestedAt = requestedAt;
  }

  public LocalDateTime getRespondedAt() {
    return respondedAt;
  }

  public void setRespondedAt(LocalDateTime respondedAt) {
    this.respondedAt = respondedAt;
  }
}
