package com.smartcampus.platform.student.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StudentRequest {
  @NotNull
  private Long userId;

  @NotBlank
  private String studentCode;

  private String department;
  private Integer yearOfStudy;
  private String section;
  private String phone;
  private String parentPhone;

  public Long getUserId() {
    return userId;
  }

  public void setUserId(Long userId) {
    this.userId = userId;
  }

  public String getStudentCode() {
    return studentCode;
  }

  public void setStudentCode(String studentCode) {
    this.studentCode = studentCode;
  }

  public String getDepartment() {
    return department;
  }

  public void setDepartment(String department) {
    this.department = department;
  }

  public Integer getYearOfStudy() {
    return yearOfStudy;
  }

  public void setYearOfStudy(Integer yearOfStudy) {
    this.yearOfStudy = yearOfStudy;
  }

  public String getSection() {
    return section;
  }

  public void setSection(String section) {
    this.section = section;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getParentPhone() {
    return parentPhone;
  }

  public void setParentPhone(String parentPhone) {
    this.parentPhone = parentPhone;
  }
}
