package com.smartcampus.platform.staff.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "faculty_subject_mapping")
public class FacultySubjectMapping {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "staff_id", nullable = false)
  private Staff staff;

  @Column(nullable = false, length = 120)
  private String subject;

  @Column(nullable = false, length = 100)
  private String department;

  @Column(nullable = false, length = 20)
  private String section;

  public FacultySubjectMapping() {}

  public FacultySubjectMapping(Staff staff, String subject, String department, String section) {
    this.staff = staff;
    this.subject = subject;
    this.department = department;
    this.section = section;
  }

  public Long getId() {
    return id;
  }

  public Staff getStaff() {
    return staff;
  }

  public void setStaff(Staff staff) {
    this.staff = staff;
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
