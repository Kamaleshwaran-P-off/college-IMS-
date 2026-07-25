package com.smartcampus.platform.student.dto;

public class StudentResponse {
  private Long id;
  private Long userId;
  private String fullName;
  private String email;
  private String studentCode;
  private String department;
  private Integer yearOfStudy;
  private String section;
  private String phone;
  private String parentPhone;

  public StudentResponse(
      Long id,
      Long userId,
      String fullName,
      String email,
      String studentCode,
      String department,
      Integer yearOfStudy,
      String section,
      String phone,
      String parentPhone
  ) {
    this.id = id;
    this.userId = userId;
    this.fullName = fullName;
    this.email = email;
    this.studentCode = studentCode;
    this.department = department;
    this.yearOfStudy = yearOfStudy;
    this.section = section;
    this.phone = phone;
    this.parentPhone = parentPhone;
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

  public String getStudentCode() {
    return studentCode;
  }

  public String getDepartment() {
    return department;
  }

  public Integer getYearOfStudy() {
    return yearOfStudy;
  }

  public String getSection() {
    return section;
  }

  public String getPhone() {
    return phone;
  }

  public String getParentPhone() {
    return parentPhone;
  }
}
