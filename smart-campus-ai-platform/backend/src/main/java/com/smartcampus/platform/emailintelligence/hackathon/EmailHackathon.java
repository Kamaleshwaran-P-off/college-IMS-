package com.smartcampus.platform.emailintelligence.hackathon;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.smartcampus.platform.gmail.entity.Email;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
    name = "email_hackathons",
    uniqueConstraints = @UniqueConstraint(columnNames = {"email_id"})
)
public class EmailHackathon {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "email_id", nullable = false)
  private Email email;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false)
  private String name;

  private LocalDate deadline;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  public EmailHackathon() {}

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

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public LocalDate getDeadline() {
    return deadline;
  }

  public void setDeadline(LocalDate deadline) {
    this.deadline = deadline;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
