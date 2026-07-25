package com.smartcampus.platform.staff.dto;

public class FacultySubjectResponse {
  private Long id;
  private Long facultyId;
  private String facultyName;
  private String subject;
  private String department;
  private String section;

  public FacultySubjectResponse(
      Long id,
      Long facultyId,
      String facultyName,
      String subject,
      String department,
      String section
  ) {
    this.id = id;
    this.facultyId = facultyId;
    this.facultyName = facultyName;
    this.subject = subject;
    this.department = department;
    this.section = section;
  }

  public Long getId() {
    return id;
  }

  public Long getFacultyId() {
    return facultyId;
  }

  public String getFacultyName() {
    return facultyName;
  }

  public String getSubject() {
    return subject;
  }

  public String getDepartment() {
    return department;
  }

  public String getSection() {
    return section;
  }
}
