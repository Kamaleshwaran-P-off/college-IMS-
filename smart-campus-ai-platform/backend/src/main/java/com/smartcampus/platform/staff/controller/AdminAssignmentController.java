package com.smartcampus.platform.staff.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartcampus.platform.staff.dto.AssignClassRequest;
import com.smartcampus.platform.staff.service.FacultyClassAssignmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin")
@Validated
public class AdminAssignmentController {
  private final FacultyClassAssignmentService assignmentService;

  public AdminAssignmentController(FacultyClassAssignmentService assignmentService) {
    this.assignmentService = assignmentService;
  }

  @PostMapping("/assign-class")
  public ResponseEntity<Map<String, Object>> assignClass(
      Authentication authentication,
      @Valid @RequestBody AssignClassRequest request
  ) {
    var classes = assignmentService.assignClasses(authentication.getName(), request);
    return ResponseEntity.ok(Map.of(
        "facultyId", request.getFacultyId(),
        "classes", classes
    ));
  }
}
