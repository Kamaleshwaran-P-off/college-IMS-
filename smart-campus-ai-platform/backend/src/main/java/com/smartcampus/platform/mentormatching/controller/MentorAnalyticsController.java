package com.smartcampus.platform.mentormatching.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartcampus.platform.mentormatching.dto.MentorAnalyticsResponse;
import com.smartcampus.platform.mentormatching.service.MentorAnalyticsService;

@RestController
@RequestMapping("/api/mentor")
public class MentorAnalyticsController {
  private final MentorAnalyticsService mentorAnalyticsService;

  public MentorAnalyticsController(MentorAnalyticsService mentorAnalyticsService) {
    this.mentorAnalyticsService = mentorAnalyticsService;
  }

  @GetMapping("/analytics")
  public List<MentorAnalyticsResponse> analytics(Authentication authentication) {
    return mentorAnalyticsService.getAnalytics(authentication.getName());
  }
}
