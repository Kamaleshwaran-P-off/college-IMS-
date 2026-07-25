package com.smartcampus.platform.staff.controller;

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

import com.smartcampus.platform.staff.dto.AssignSubjectRequest;
import com.smartcampus.platform.staff.dto.FacultySubjectResponse;
import com.smartcampus.platform.staff.service.FacultySubjectAssignmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@Validated
public class FacultySubjectController {
  private final FacultySubjectAssignmentService subjectAssignmentService;

  public FacultySubjectController(FacultySubjectAssignmentService subjectAssignmentService) {
    this.subjectAssignmentService = subjectAssignmentService;
  }

  @PostMapping("/admin/assign-subject")
  public ResponseEntity<FacultySubjectResponse> assignSubject(
      Authentication authentication,
      @Valid @RequestBody AssignSubjectRequest request
  ) {
    FacultySubjectResponse response = subjectAssignmentService.assign(authentication.getName(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/faculty/subjects")
  public List<FacultySubjectResponse> getFacultySubjects(Authentication authentication) {
    return subjectAssignmentService.getForFaculty(authentication.getName());
  }
}
