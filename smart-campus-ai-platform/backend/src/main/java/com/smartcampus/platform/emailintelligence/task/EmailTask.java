package com.smartcampus.platform.emailintelligence.task;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.smartcampus.platform.emailintelligence.EmailPriority;
import com.smartcampus.platform.gmail.entity.Email;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "email_tasks",
    uniqueConstraints = @UniqueConstraint(columnNames = {"email_id"})
)
public class EmailTask {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "email_id", nullable = false)
  private Email email;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false)
  private String title;

  private LocalDate deadline;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EmailPriority priority;

  @Column(nullable = false)
  private boolean completed;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  public EmailTask() {}

  public Long getId() {
    return id;
  }

  public Email getEmail() {
    return email;
  }

  public void setEmail(Email email) {
    this.email = email;
  }

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public LocalDate getDeadline() {
    return deadline;
  }

  public void setDeadline(LocalDate deadline) {
    this.deadline = deadline;
  }

  public EmailPriority getPriority() {
    return priority;
  }

  public void setPriority(EmailPriority priority) {
    this.priority = priority;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
