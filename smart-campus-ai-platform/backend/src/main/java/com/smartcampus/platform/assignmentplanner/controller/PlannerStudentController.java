package com.smartcampus.platform.assignmentplanner.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.assignmentplanner.dto.PlannerAssignmentResponse;
import com.smartcampus.platform.assignmentplanner.service.AssignmentPlannerService;
import com.smartcampus.platform.auth.repository.UserRepository;

@RestController
@RequestMapping("/api/student/assignments")
public class PlannerStudentController {
  private final AssignmentPlannerService plannerService;
  private final UserRepository userRepository;

  public PlannerStudentController(AssignmentPlannerService plannerService, UserRepository userRepository) {
    this.plannerService = plannerService;
    this.userRepository = userRepository;
  }

  @GetMapping
  public List<PlannerAssignmentResponse> list(Authentication authentication) {
    var user = userRepository.findByEmail(authentication.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    return plannerService.getAssignmentsForStudent(user);
  }
}
