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

import com.smartcampus.platform.mentormatching.dto.StudentPreferenceRequest;
import com.smartcampus.platform.mentormatching.dto.StudentPreferenceResponse;
import com.smartcampus.platform.mentormatching.service.StudentPreferenceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/student")
@Validated
public class StudentPreferenceController {
  private final StudentPreferenceService preferenceService;

  public StudentPreferenceController(StudentPreferenceService preferenceService) {
    this.preferenceService = preferenceService;
  }

  @PostMapping("/preferences")
  public ResponseEntity<StudentPreferenceResponse> upsert(
      Authentication authentication,
      @Valid @RequestBody StudentPreferenceRequest request
  ) {
    StudentPreferenceResponse response = preferenceService.upsert(authentication.getName(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/preferences")
  public StudentPreferenceResponse getPreferences(Authentication authentication) {
    return preferenceService.getPreferences(authentication.getName());
  }
}
