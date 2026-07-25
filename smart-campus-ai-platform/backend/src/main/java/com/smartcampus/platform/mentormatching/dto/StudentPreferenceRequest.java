package com.smartcampus.platform.mentormatching.dto;

import jakarta.validation.constraints.NotBlank;

public class StudentPreferenceRequest {
  @NotBlank
  private String requiredSkills;

  private String learningGoals;

  private String mentorType;

  private String availability;

  public String getRequiredSkills() {
    return requiredSkills;
  }

  public void setRequiredSkills(String requiredSkills) {
    this.requiredSkills = requiredSkills;
  }

  public String getLearningGoals() {
    return learningGoals;
  }

  public void setLearningGoals(String learningGoals) {
    this.learningGoals = learningGoals;
  }

  public String getMentorType() {
    return mentorType;
  }

  public void setMentorType(String mentorType) {
    this.mentorType = mentorType;
  }

  public String getAvailability() {
    return availability;
  }

  public void setAvailability(String availability) {
    this.availability = availability;
  }
}
