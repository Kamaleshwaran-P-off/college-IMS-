package com.smartcampus.platform.dashboard.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.dashboard.dto.FacultyDashboardResponse;
import com.smartcampus.platform.dashboard.service.DashboardService;

@RestController
@RequestMapping("/api/faculty")
public class FacultyDashboardController {
  private final DashboardService dashboardService;
  private final UserRepository userRepository;

  public FacultyDashboardController(DashboardService dashboardService, UserRepository userRepository) {
    this.dashboardService = dashboardService;
    this.userRepository = userRepository;
  }

  @GetMapping("/dashboard")
  public ResponseEntity<FacultyDashboardResponse> getDashboard(Authentication authentication) {
    String email = authentication.getName();
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    return ResponseEntity.ok(dashboardService.buildFacultyDashboard(user));
  }
}
