package com.smartcampus.platform.profile.dto;

import java.util.List;

public class UserProfileUpdateRequest {
  private String name;
  private String phone;
  private String department;
  private String section;
  private String registerNumber;
  private List<String> interestedSkills;
  private String staffId;
  private List<String> skills;
  private Integer experienceYears;
  private String bio;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public String getSection() {
    return section;
  }

  public void setSection(String section) {
    this.section = section;
  }

  public String getRegisterNumber() {
    return registerNumber;
  }

  public void setRegisterNumber(String registerNumber) {
    this.registerNumber = registerNumber;
  }

  public List<String> getInterestedSkills() {
    return interestedSkills;
  }

  public void setInterestedSkills(List<String> interestedSkills) {
    this.interestedSkills = interestedSkills;
  }

  public String getStaffId() {
    return staffId;
  }

  public void setStaffId(String staffId) {
    this.staffId = staffId;
  }

  public List<String> getSkills() {
    return skills;
  }

  public void setSkills(List<String> skills) {
    this.skills = skills;
  }

  public Integer getExperienceYears() {
    return experienceYears;
  }

  public void setExperienceYears(Integer experienceYears) {
    this.experienceYears = experienceYears;
  }

  public String getBio() {
    return bio;
  }

  public void setBio(String bio) {
    this.bio = bio;
  }
}
