package com.smartcampus.platform.assignmentplanner.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.assignmentplanner.dto.PlannerAssignmentRequest;
import com.smartcampus.platform.assignmentplanner.dto.PlannerAssignmentResponse;
import com.smartcampus.platform.assignmentplanner.service.AssignmentPlannerService;
import com.smartcampus.platform.auth.repository.UserRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/faculty/assignments")
@Validated
public class PlannerFacultyController {
  private final AssignmentPlannerService plannerService;
  private final UserRepository userRepository;

  public PlannerFacultyController(AssignmentPlannerService plannerService, UserRepository userRepository) {
    this.plannerService = plannerService;
    this.userRepository = userRepository;
  }

  @PostMapping
  public ResponseEntity<PlannerAssignmentResponse> create(
      Authentication authentication,
      @Valid @RequestBody PlannerAssignmentRequest request
  ) {
    var user = userRepository.findByEmail(authentication.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    return ResponseEntity.status(HttpStatus.CREATED).body(plannerService.createAssignment(request, user));
  }

  @GetMapping
  public List<PlannerAssignmentResponse> list(Authentication authentication) {
    var user = userRepository.findByEmail(authentication.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    return plannerService.getAssignmentsForFaculty(user);
  }
}
