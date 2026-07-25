package com.smartcampus.platform.staff.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AssignSubjectRequest {
  @NotNull
  private Long facultyId;

  @NotBlank
  private String subject;

  @NotBlank
  private String department;

  @NotBlank
  private String section;

  public Long getFacultyId() {
    return facultyId;
  }

  public void setFacultyId(Long facultyId) {
    this.facultyId = facultyId;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
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
}
