package com.smartcampus.platform.mentormatching.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartcampus.platform.mentormatching.dto.FacultySkillRequest;
import com.smartcampus.platform.mentormatching.dto.FacultySkillResponse;
import com.smartcampus.platform.mentormatching.service.FacultySkillService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/faculty")
@Validated
public class FacultySkillController {
  private final FacultySkillService facultySkillService;

  public FacultySkillController(FacultySkillService facultySkillService) {
    this.facultySkillService = facultySkillService;
  }

  @PostMapping("/skills")
  public ResponseEntity<FacultySkillResponse> upsertSkills(
      Authentication authentication,
      @Valid @RequestBody FacultySkillRequest request
  ) {
    FacultySkillResponse response = facultySkillService.upsert(authentication.getName(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/profile")
  public FacultySkillResponse getProfile(Authentication authentication) {
    return facultySkillService.getProfile(authentication.getName());
  }
}
