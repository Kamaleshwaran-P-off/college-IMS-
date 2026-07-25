package com.smartcampus.platform.mentormatching.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartcampus.platform.mentormatching.dto.MentorRequestCreateRequest;
import com.smartcampus.platform.mentormatching.dto.MentorRequestResponse;
import com.smartcampus.platform.mentormatching.dto.MentorRequestStatusRequest;
import com.smartcampus.platform.mentormatching.service.MentorRequestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/request")
@Validated
public class MentorRequestController {
  private final MentorRequestService requestService;

  public MentorRequestController(MentorRequestService requestService) {
    this.requestService = requestService;
  }

  @PostMapping
  public ResponseEntity<MentorRequestResponse> create(
      Authentication authentication,
      @Valid @RequestBody MentorRequestCreateRequest request
  ) {
    MentorRequestResponse response = requestService.createRequest(authentication.getName(), request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @PutMapping("/status")
  public MentorRequestResponse updateStatus(
      Authentication authentication,
      @Valid @RequestBody MentorRequestStatusRequest request
  ) {
    return requestService.updateStatus(authentication.getName(), request);
  }

  @GetMapping
  public List<MentorRequestResponse> list(Authentication authentication) {
    return requestService.listForUser(authentication.getName());
  }
}
