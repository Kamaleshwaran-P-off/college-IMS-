package com.smartcampus.platform.parentalert.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.smartcampus.platform.student.entity.Student;
import jakarta.persistence.*;

@Entity
@Table(name = "parent_alerts", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "alert_date"})
})
public class ParentAlert {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @Column(name = "alert_date", nullable = false)
  private LocalDate alertDate;

  @Column(nullable = false)
  private double attendancePercent;

  @Column(nullable = false)
  private String channel;

  @Lob
  private String message;

  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  public ParentAlert() {}

  public ParentAlert(Student student, LocalDate alertDate, double attendancePercent, String channel, String message) {
    this.student = student;
    this.alertDate = alertDate;
    this.attendancePercent = attendancePercent;
    this.channel = channel;
    this.message = message;
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

  public LocalDate getAlertDate() {
    return alertDate;
  }

  public double getAttendancePercent() {
    return attendancePercent;
  }

  public String getChannel() {
    return channel;
  }

  public String getMessage() {
    return message;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
