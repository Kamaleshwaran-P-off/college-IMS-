package com.smartcampus.platform.mentormatching.dto;

import com.smartcampus.platform.mentormatching.entity.ProficiencyLevel;

public class FacultySkillResponse {
  private Long id;
  private Long staffId;
  private String staffName;
  private String department;
  private String skills;
  private ProficiencyLevel proficiencyLevel;
  private String availability;
  private String bio;

  public FacultySkillResponse(
      Long id,
      Long staffId,
      String staffName,
      String department,
      String skills,
      ProficiencyLevel proficiencyLevel,
      String availability,
      String bio
  ) {
    this.id = id;
    this.staffId = staffId;
    this.staffName = staffName;
    this.department = department;
    this.skills = skills;
    this.proficiencyLevel = proficiencyLevel;
    this.availability = availability;
    this.bio = bio;
  }

  public Long getId() {
    return id;
  }

  public Long getStaffId() {
    return staffId;
  }

  public String getStaffName() {
    return staffName;
  }

  public String getDepartment() {
    return department;
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
}
