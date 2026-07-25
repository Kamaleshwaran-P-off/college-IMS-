package com.smartcampus.platform.mentormatching.entity;

import java.time.LocalDateTime;

import com.smartcampus.platform.staff.entity.Staff;
import jakarta.persistence.*;

@Entity
@Table(name = "faculty_skills")
public class FacultySkill {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "staff_id", nullable = false, unique = true)
  private Staff staff;

  @Lob
  private String skills;

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  private ProficiencyLevel proficiencyLevel;

  @Column(length = 255)
  private String availability;

  @Lob
  private String bio;

  @Column(nullable = false)
  private LocalDateTime updatedAt;

  public FacultySkill() {}

  public FacultySkill(
      Staff staff,
      String skills,
      ProficiencyLevel proficiencyLevel,
      String availability,
      String bio,
      LocalDateTime updatedAt
  ) {
    this.staff = staff;
    this.skills = skills;
    this.proficiencyLevel = proficiencyLevel;
    this.availability = availability;
    this.bio = bio;
    this.updatedAt = updatedAt;
  }

  public Long getId() {
    return id;
  }

  public Staff getStaff() {
    return staff;
  }

  public void setStaff(Staff staff) {
    this.staff = staff;
  }

  public String getSkills() {
    return skills;
  }

  public void setSkills(String skills) {
    this.skills = skills;
  }

  public ProficiencyLevel getProficiencyLevel() {
    return proficiencyLevel;
  }

  public void setProficiencyLevel(ProficiencyLevel proficiencyLevel) {
    this.proficiencyLevel = proficiencyLevel;
  }

  public String getAvailability() {
    return availability;
  }

  public void setAvailability(String availability) {
    this.availability = availability;
  }

  public String getBio() {
    return bio;
  }

  public void setBio(String bio) {
    this.bio = bio;
  }

  public LocalDateTime getUpdatedAt() {
    return updatedAt;
  }

  public void setUpdatedAt(LocalDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }
}
