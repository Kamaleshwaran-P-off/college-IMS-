package com.smartcampus.platform.profile.dto;

import java.util.List;

public class UserProfileResponse {
  private Long id;
  private String name;
  private String email;
  private String role;
  private String profileImageUrl;
  private StudentProfileDetails student;
  private FacultyProfileDetails faculty;

  public UserProfileResponse(
      Long id,
      String name,
      String email,
      String role,
      String profileImageUrl,
      StudentProfileDetails student,
      FacultyProfileDetails faculty
  ) {
    this.id = id;
    this.name = name;
    this.email = email;
    this.role = role;
    this.profileImageUrl = profileImageUrl;
    this.student = student;
    this.faculty = faculty;
  }

  public Long getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public String getEmail() {
    return email;
  }

  public String getRole() {
    return role;
  }

  public String getProfileImageUrl() {
    return profileImageUrl;
  }

  public StudentProfileDetails getStudent() {
    return student;
  }

  public FacultyProfileDetails getFaculty() {
    return faculty;
  }

  public static class StudentProfileDetails {
    private String registerNumber;
    private String phone;
    private String department;
    private String section;
    private List<String> interestedSkills;

    public StudentProfileDetails(
        String registerNumber,
        String phone,
        String department,
        String section,
        List<String> interestedSkills
    ) {
      this.registerNumber = registerNumber;
      this.phone = phone;
      this.department = department;
      this.section = section;
      this.interestedSkills = interestedSkills;
    }

    public String getRegisterNumber() {
      return registerNumber;
    }

    public String getPhone() {
      return phone;
    }

    public String getDepartment() {
      return department;
    }

    public String getSection() {
      return section;
    }

    public List<String> getInterestedSkills() {
      return interestedSkills;
    }
  }

  public static class FacultyProfileDetails {
    private String staffId;
    private String department;
    private List<String> skills;
    private Integer experienceYears;
    private String bio;

    public FacultyProfileDetails(
        String staffId,
        String department,
        List<String> skills,
        Integer experienceYears,
        String bio
    ) {
      this.staffId = staffId;
      this.department = department;
      this.skills = skills;
      this.experienceYears = experienceYears;
      this.bio = bio;
    }

    public String getStaffId() {
      return staffId;
    }

    public String getDepartment() {
      return department;
    }

    public List<String> getSkills() {
      return skills;
    }

    public Integer getExperienceYears() {
      return experienceYears;
    }

    public String getBio() {
      return bio;
    }
  }
}
