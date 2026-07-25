package com.smartcampus.platform.staff.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartcampus.platform.staff.service.FacultyClassAssignmentService;
import com.smartcampus.platform.staff.dto.FacultyClassSummary;

@RestController
@RequestMapping("/api/faculty")
public class FacultyClassController {
  private final FacultyClassAssignmentService assignmentService;

  public FacultyClassController(FacultyClassAssignmentService assignmentService) {
    this.assignmentService = assignmentService;
  }

  @GetMapping("/classes")
  public List<String> getAssignedClasses(Authentication authentication) {
    return assignmentService.getAssignedClasses(authentication.getName());
  }

  @GetMapping("/classes/details")
  public List<FacultyClassSummary> getAssignedClassDetails(Authentication authentication) {
    return assignmentService.getAssignedClassDetails(authentication.getName());
  }

  @GetMapping("/class/{classId}")
  public FacultyClassSummary getClassForFaculty(
      @PathVariable Long classId,
      Authentication authentication
  ) {
    return assignmentService.getAssignedClass(authentication.getName(), classId);
  }
}
