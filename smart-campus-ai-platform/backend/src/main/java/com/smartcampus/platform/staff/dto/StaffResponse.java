package com.smartcampus.platform.staff.dto;

public class StaffResponse {
  private Long id;
  private Long userId;
  private String fullName;
  private String email;
  private String staffCode;
  private String department;
  private String designation;
  private String phone;
  private String assignedClasses;
  private String skills;
  private String interests;

  public StaffResponse(
      Long id,
      Long userId,
      String fullName,
      String email,
      String staffCode,
      String department,
      String designation,
      String phone,
      String assignedClasses,
      String skills,
      String interests
  ) {
    this.id = id;
    this.userId = userId;
    this.fullName = fullName;
    this.email = email;
    this.staffCode = staffCode;
    this.department = department;
    this.designation = designation;
    this.phone = phone;
    this.assignedClasses = assignedClasses;
    this.skills = skills;
    this.interests = interests;
  }

  public Long getId() {
    return id;
  }

  public Long getUserId() {
    return userId;
  }

  public String getFullName() {
    return fullName;
  }

  public String getEmail() {
    return email;
  }

  public String getStaffCode() {
    return staffCode;
  }

  public String getDepartment() {
    return department;
  }

  public String getDesignation() {
    return designation;
  }

  public String getPhone() {
    return phone;
  }

  public String getAssignedClasses() {
    return assignedClasses;
  }

  public String getSkills() {
    return skills;
  }

  public String getInterests() {
    return interests;
  }
}
