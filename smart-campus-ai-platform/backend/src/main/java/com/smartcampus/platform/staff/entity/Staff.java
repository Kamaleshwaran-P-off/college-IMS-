package com.smartcampus.platform.staff.entity;

import com.smartcampus.platform.auth.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "staff")
public class Staff {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @NotBlank
  @Column(nullable = false)
  private String staffCode;

  @Column(length = 100)
  private String department;

  @Column(length = 100)
  private String designation;

  @Column(length = 20)
  private String phone;

  @Column(length = 255)
  private String assignedClasses;

  @Lob
  private String skills;

  @Lob
  private String interests;

  private Integer experienceYears;

  public Staff() {}

  public Staff(
      User user,
      String staffCode,
      String department,
      String designation,
      String phone,
      String assignedClasses,
      String skills,
      String interests
  ) {
    this.user = user;
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

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }

  public String getStaffCode() {
    return staffCode;
  }

  public void setStaffCode(String staffCode) {
    this.staffCode = staffCode;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public String getDesignation() {
    return designation;
  }

  public void setDesignation(String designation) {
    this.designation = designation;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getAssignedClasses() {
    return assignedClasses;
  }

  public void setAssignedClasses(String assignedClasses) {
    this.assignedClasses = assignedClasses;
  }

  public String getSkills() {
    return skills;
  }

  public void setSkills(String skills) {
    this.skills = skills;
  }

  public String getInterests() {
    return interests;
  }

  public void setInterests(String interests) {
    this.interests = interests;
  }

  public Integer getExperienceYears() {
    return experienceYears;
  }

  public void setExperienceYears(Integer experienceYears) {
    this.experienceYears = experienceYears;
  }
}
