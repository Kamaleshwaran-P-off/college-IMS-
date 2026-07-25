package com.smartcampus.platform.staff.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "faculty_class_mapping")
public class FacultyClassMapping {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "staff_id", nullable = false)
  private Staff staff;

  @Column(nullable = false, length = 50)
  private String className;

  @Column(length = 100)
  private String department;

  @Column(length = 20)
  private String section;

  public FacultyClassMapping() {}

  public FacultyClassMapping(Staff staff, String className) {
    this.staff = staff;
    this.className = className;
  }

  public FacultyClassMapping(Staff staff, String className, String department, String section) {
    this.staff = staff;
    this.className = className;
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

  public String getClassName() {
    return className;
  }

  public void setClassName(String className) {
    this.className = className;
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
