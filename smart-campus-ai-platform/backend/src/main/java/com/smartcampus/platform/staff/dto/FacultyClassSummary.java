package com.smartcampus.platform.staff.dto;

public class FacultyClassSummary {
  private Long id;
  private String className;
  private String department;
  private String section;

  public FacultyClassSummary(Long id, String className, String department, String section) {
    this.id = id;
    this.className = className;
    this.department = department;
    this.section = section;
  }

  public Long getId() {
    return id;
  }

  public String getClassName() {
    return className;
  }

  public String getDepartment() {
    return department;
  }

  public String getSection() {
    return section;
  }
}
