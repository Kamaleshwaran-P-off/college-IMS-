package com.smartcampus.platform.mentor.entity;

import java.time.LocalDateTime;

import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.student.entity.Student;
import jakarta.persistence.*;

@Entity
@Table(name = "mentor_assignments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id"})
})
public class MentorAssignment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @ManyToOne(optional = false)
  @JoinColumn(name = "mentor_id", nullable = false)
  private Staff mentor;

  @ManyToOne
  @JoinColumn(name = "assigned_by")
  private User assignedBy;

  @Column(nullable = false, updatable = false)
  private LocalDateTime assignedAt;

  public MentorAssignment() {}

  public MentorAssignment(Student student, Staff mentor, User assignedBy) {
    this.student = student;
    this.mentor = mentor;
    this.assignedBy = assignedBy;
  }

  @PrePersist
  public void onCreate() {
    if (assignedAt == null) {
      assignedAt = LocalDateTime.now();
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

  public Staff getMentor() {
    return mentor;
  }

  public void setMentor(Staff mentor) {
    this.mentor = mentor;
  }

  public User getAssignedBy() {
    return assignedBy;
  }

  public void setAssignedBy(User assignedBy) {
    this.assignedBy = assignedBy;
  }

  public LocalDateTime getAssignedAt() {
    return assignedAt;
  }

  public void setAssignedAt(LocalDateTime assignedAt) {
    this.assignedAt = assignedAt;
  }
}
