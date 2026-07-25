package com.smartcampus.platform.emailintelligence;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "email_ai_insights",
    uniqueConstraints = @UniqueConstraint(columnNames = {"email_id"})
)
public class EmailInsight {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "email_id", nullable = false)
  private Email email;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(nullable = false)
  private String category;

  @Lob
  @Column(nullable = false)
  private String summary;

  private LocalDate deadline;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EmailPriority priority;

  @Column(name = "action_required", nullable = false)
  private boolean actionRequired;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  public EmailInsight() {}

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

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
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

  public boolean isActionRequired() {
    return actionRequired;
  }

  public void setActionRequired(boolean actionRequired) {
    this.actionRequired = actionRequired;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
