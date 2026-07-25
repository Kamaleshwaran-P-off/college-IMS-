package com.smartcampus.platform.auth.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.smartcampus.platform.auth.dto.AuthRequest;
import com.smartcampus.platform.auth.dto.AuthResponse;
import com.smartcampus.platform.auth.dto.SignupRequest;
import com.smartcampus.platform.auth.dto.SignupResponse;
import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.auth.security.JwtService;
import com.smartcampus.platform.staff.service.StaffProfileService;
import com.smartcampus.platform.student.service.StudentProfileService;

@Service
public class AuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtService jwtService;
  private final StudentProfileService studentProfileService;
  private final StaffProfileService staffProfileService;

  public AuthService(
      UserRepository userRepository,
      PasswordEncoder passwordEncoder,
      AuthenticationManager authenticationManager,
      JwtService jwtService,
      StudentProfileService studentProfileService,
      StaffProfileService staffProfileService
  ) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.authenticationManager = authenticationManager;
    this.jwtService = jwtService;
    this.studentProfileService = studentProfileService;
    this.staffProfileService = staffProfileService;
  }

  public SignupResponse signup(SignupRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("Email already registered");
    }

    User user = new User(
        request.getFullName(),
        request.getEmail(),
        passwordEncoder.encode(request.getPassword()),
        request.getRole()
    );

    userRepository.save(user);
    return new SignupResponse("Account created successfully");
  }

  public AuthResponse login(AuthRequest request) {
    Authentication authentication;
    try {
      authentication = authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
      );
    } catch (AuthenticationException ex) {
      throw new IllegalArgumentException("Invalid credentials");
    }

    User user = userRepository.findByEmail(authentication.getName())
        .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

    if (user.getRole() == Role.STUDENT) {
      studentProfileService.ensureForUser(user);
    } else if (user.getRole() == Role.STAFF) {
      staffProfileService.ensureForUser(user);
    }

    String token = jwtService.generateToken(user);
    String role = "ROLE_" + user.getRole().name();
    return new AuthResponse(token, role, user.getId(), user.getEmail(), user.getFullName());
  }
}
