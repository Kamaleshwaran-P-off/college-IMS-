package com.smartcampus.platform.mentormatching.dto;

import com.smartcampus.platform.mentormatching.entity.ProficiencyLevel;
import jakarta.validation.constraints.NotBlank;

public class FacultySkillRequest {
  @NotBlank
  private String skills;

  private ProficiencyLevel proficiencyLevel;

  private String availability;

  private String bio;

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
}
