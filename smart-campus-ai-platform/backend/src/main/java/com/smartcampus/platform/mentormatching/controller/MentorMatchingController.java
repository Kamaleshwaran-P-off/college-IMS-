package com.smartcampus.platform.mentormatching.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartcampus.platform.mentormatching.dto.MentorMatchResponse;
import com.smartcampus.platform.mentormatching.service.MatchingService;

import jakarta.validation.constraints.NotNull;

@RestController
@RequestMapping("/api")
@Validated
public class MentorMatchingController {
  private final MatchingService matchingService;

  public MentorMatchingController(MatchingService matchingService) {
    this.matchingService = matchingService;
  }

  @GetMapping("/student/matches")
  public List<MentorMatchResponse> getStudentMatches(Authentication authentication) {
    return matchingService.getTopMentors(authentication.getName());
  }

  @PostMapping("/match/run")
  public ResponseEntity<List<MentorMatchResponse>> runMatch(
      Authentication authentication,
      @RequestBody MatchRunRequest request
  ) {
    List<MentorMatchResponse> matches = matchingService.runForStudent(request.getStudentId(), authentication.getName());
    return ResponseEntity.status(HttpStatus.CREATED).body(matches);
  }

  @GetMapping("/match/{studentId}")
  public List<MentorMatchResponse> getMatches(
      Authentication authentication,
      @PathVariable Long studentId
  ) {
    return matchingService.runForStudent(studentId, authentication.getName());
  }

  public static class MatchRunRequest {
    @NotNull
    private Long studentId;

    public Long getStudentId() {
      return studentId;
    }

    public void setStudentId(Long studentId) {
      this.studentId = studentId;
    }
  }
}
