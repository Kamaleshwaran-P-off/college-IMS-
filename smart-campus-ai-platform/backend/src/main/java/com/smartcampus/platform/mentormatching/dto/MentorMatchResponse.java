package com.smartcampus.platform.mentormatching.dto;

import com.smartcampus.platform.mentormatching.entity.ProficiencyLevel;

public class MentorMatchResponse {
  private Long mentorId;
  private String mentorName;
  private String mentorDepartment;
  private String skills;
  private ProficiencyLevel proficiencyLevel;
  private String availability;
  private String bio;
  private Double score;

  public MentorMatchResponse(
      Long mentorId,
      String mentorName,
      String mentorDepartment,
      String skills,
      ProficiencyLevel proficiencyLevel,
      String availability,
      String bio,
      Double score
  ) {
    this.mentorId = mentorId;
    this.mentorName = mentorName;
    this.mentorDepartment = mentorDepartment;
    this.skills = skills;
    this.proficiencyLevel = proficiencyLevel;
    this.availability = availability;
    this.bio = bio;
    this.score = score;
  }

  public Long getMentorId() {
    return mentorId;
  }

  public String getMentorName() {
    return mentorName;
  }

  public String getMentorDepartment() {
    return mentorDepartment;
  }

  public String getSkills() {
    return skills;
  }

  public ProficiencyLevel getProficiencyLevel() {
    return proficiencyLevel;
  }

  public String getAvailability() {
    return availability;
  }

  public String getBio() {
    return bio;
  }

  public Double getScore() {
    return score;
  }
}
