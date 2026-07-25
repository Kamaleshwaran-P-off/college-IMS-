package com.smartcampus.platform.learning.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "learning_profiles", uniqueConstraints = @UniqueConstraint(columnNames = {"student_id"}))
public class LearningProfile {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "student_id", nullable = false)
  private Long studentId;

  private int points;

  private int streak;

  private LocalDate lastActive;

  @Column(length = 500)
  private String badges;

  public LearningProfile() {}

  public LearningProfile(Long studentId, int points, int streak, LocalDate lastActive, String badges) {
    this.studentId = studentId;
    this.points = points;
    this.streak = streak;
    this.lastActive = lastActive;
    this.badges = badges;
  }

  public Long getId() {
    return id;
  }

  public Long getStudentId() {
    return studentId;
  }

  public void setStudentId(Long studentId) {
    this.studentId = studentId;
  }

  public int getPoints() {
    return points;
  }

  public void setPoints(int points) {
    this.points = points;
  }

  public int getStreak() {
    return streak;
  }

  public void setStreak(int streak) {
    this.streak = streak;
  }

  public LocalDate getLastActive() {
    return lastActive;
  }

  public void setLastActive(LocalDate lastActive) {
    this.lastActive = lastActive;
  }

  public String getBadges() {
    return badges;
  }

  public void setBadges(String badges) {
    this.badges = badges;
  }
}
