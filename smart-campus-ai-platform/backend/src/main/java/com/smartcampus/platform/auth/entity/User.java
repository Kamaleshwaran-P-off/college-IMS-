package com.smartcampus.platform.auth.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "users")
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(nullable = false)
  private String fullName;

  @Email
  @NotBlank
  @Column(nullable = false, unique = true)
  private String email;

  @NotBlank
  @Column(nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Role role;

  private String profileImageUrl;

  @Column(length = 100)
  private String profileImageContentType;

  @Lob
  private byte[] profileImageData;

  public User() {}

  public User(String fullName, String email, String password, Role role) {
    this.fullName = fullName;
    this.email = email;
    this.password = password;
    this.role = role;
  }

  public Long getId() {
    return id;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String fullName) {
    this.fullName = fullName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(Role role) {
    this.role = role;
  }

  public String getProfileImageUrl() {
    return profileImageUrl;
  }

  public void setProfileImageUrl(String profileImageUrl) {
    this.profileImageUrl = profileImageUrl;
  }

  public String getProfileImageContentType() {
    return profileImageContentType;
  }

  public void setProfileImageContentType(String profileImageContentType) {
    this.profileImageContentType = profileImageContentType;
  }

  public byte[] getProfileImageData() {
    return profileImageData;
  }

  public void setProfileImageData(byte[] profileImageData) {
    this.profileImageData = profileImageData;
  }
}
