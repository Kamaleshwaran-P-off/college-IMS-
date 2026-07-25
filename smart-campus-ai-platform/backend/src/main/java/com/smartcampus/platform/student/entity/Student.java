package com.smartcampus.platform.student.entity;

import com.smartcampus.platform.auth.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "students")
public class Student {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @NotBlank
  @Column(nullable = false)
  private String studentCode;

  @Column(length = 100)
  private String department;

  private Integer yearOfStudy;

  @Column(length = 20)
  private String section;

  @Column(length = 20)
  private String phone;

  @Column(length = 25)
  private String parentPhone;

  public Student() {}

  public Student(
      User user,
      String studentCode,
      String department,
      Integer yearOfStudy,
      String section,
      String phone,
      String parentPhone
  ) {
    this.user = user;
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

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
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
