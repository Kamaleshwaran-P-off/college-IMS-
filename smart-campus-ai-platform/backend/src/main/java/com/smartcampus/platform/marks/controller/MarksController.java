package com.smartcampus.platform.marks.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartcampus.platform.marks.dto.MarksRequest;
import com.smartcampus.platform.marks.dto.MarksResponse;
import com.smartcampus.platform.marks.service.MarksService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/marks")
@Validated
public class MarksController {
  private final MarksService marksService;

  public MarksController(MarksService marksService) {
    this.marksService = marksService;
  }

  @PostMapping
  public ResponseEntity<MarksResponse> upsertMarks(
      Authentication authentication,
      @Valid @RequestBody MarksRequest request
  ) {
    MarksResponse response = marksService.upsertMarks(request, authentication.getName());
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/mine")
  public List<MarksResponse> getStudentMarks(Authentication authentication) {
    return marksService.getMarksForStudent(authentication.getName());
  }

  @GetMapping
  public List<MarksResponse> getClassMarks(
      Authentication authentication,
      @RequestParam String subject,
      @RequestParam String department,
      @RequestParam String section
  ) {
    return marksService.getMarksForClass(authentication.getName(), subject, department, section);
  }
}
