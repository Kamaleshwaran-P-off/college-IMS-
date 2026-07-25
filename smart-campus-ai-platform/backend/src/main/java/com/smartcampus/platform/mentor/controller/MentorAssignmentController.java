package com.smartcampus.platform.mentor.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.smartcampus.platform.mentor.dto.MentorAssignRequest;
import com.smartcampus.platform.mentor.dto.MentorAnalyticsResponse;
import com.smartcampus.platform.mentor.dto.MentorAssignmentResponse;
import com.smartcampus.platform.mentor.dto.MentorAutoAllocateRequest;
import com.smartcampus.platform.mentor.service.MentorAssignmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/mentors")
@Validated
public class MentorAssignmentController {
  private final MentorAssignmentService mentorAssignmentService;

  public MentorAssignmentController(MentorAssignmentService mentorAssignmentService) {
    this.mentorAssignmentService = mentorAssignmentService;
  }

  @PostMapping("/assign")
  public ResponseEntity<MentorAssignmentResponse> assign(@Valid @RequestBody MentorAssignRequest request) {
    return ResponseEntity.status(HttpStatus.OK).body(mentorAssignmentService.assignMentor(request));
  }

  @PostMapping("/auto-allocate")
  public ResponseEntity<List<MentorAssignmentResponse>> autoAllocate(
      @RequestBody(required = false) MentorAutoAllocateRequest request
  ) {
    return ResponseEntity.status(HttpStatus.OK).body(mentorAssignmentService.autoAllocate(request));
  }

  @GetMapping
  public List<MentorAssignmentResponse> list(
      @RequestParam(required = false) Long studentId,
      @RequestParam(required = false) Long mentorId
  ) {
    return mentorAssignmentService.findAll(studentId, mentorId);
  }

  @GetMapping("/analytics")
  public List<MentorAnalyticsResponse> analytics(@RequestParam Long adminUserId) {
    return mentorAssignmentService.getAnalytics(adminUserId);
  }
}
