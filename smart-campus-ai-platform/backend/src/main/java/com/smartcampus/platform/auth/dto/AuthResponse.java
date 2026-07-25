package com.smartcampus.platform.auth.dto;

public class AuthResponse {
  private String token;
  private String role;
  private Long userId;
  private String email;
  private String fullName;

  public AuthResponse(String token, String role, Long userId, String email, String fullName) {
    this.token = token;
    this.role = role;
    this.userId = userId;
    this.email = email;
    this.fullName = fullName;
  }

  public String getToken() {
    return token;
  }

  public String getRole() {
    return role;
  }

  public Long getUserId() {
    return userId;
  }

  public String getEmail() {
    return email;
  }

  public String getFullName() {
    return fullName;
  }
}
