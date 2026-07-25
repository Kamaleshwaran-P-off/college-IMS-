package com.smartcampus.platform.staff.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class AssignClassRequest {
  @NotNull
  private Long facultyId;

  @NotEmpty
  private List<String> classes;

  public AssignClassRequest() {}

  public AssignClassRequest(Long facultyId, List<String> classes) {
    this.facultyId = facultyId;
    this.classes = classes;
  }

  public Long getFacultyId() {
    return facultyId;
  }

  public void setFacultyId(Long facultyId) {
    this.facultyId = facultyId;
  }

  public List<String> getClasses() {
    return classes;
  }

  public void setClasses(List<String> classes) {
    this.classes = classes;
  }
}
