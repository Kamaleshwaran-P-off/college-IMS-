package com.smartcampus.platform.happenstance.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartcampus.platform.happenstance.dto.HappenstanceAnalyticsResponse;
import com.smartcampus.platform.happenstance.dto.HappenstanceAdminAnalyticsResponse;
import com.smartcampus.platform.happenstance.dto.HappenstanceClickRequest;
import com.smartcampus.platform.happenstance.dto.HappenstanceOpportunityResponse;
import com.smartcampus.platform.happenstance.dto.HappenstanceSaveRequest;
import com.smartcampus.platform.happenstance.dto.HappenstanceSaveResponse;
import com.smartcampus.platform.happenstance.service.HappenstanceService;

@RestController
@RequestMapping("/api/happenstance")
public class HappenstanceController {
  private final HappenstanceService happenstanceService;

  public HappenstanceController(HappenstanceService happenstanceService) {
    this.happenstanceService = happenstanceService;
  }

  @GetMapping
  public List<HappenstanceOpportunityResponse> getFeed(Authentication authentication) {
    return happenstanceService.getFeed(authentication.getName());
  }

  @PostMapping("/save")
  public ResponseEntity<HappenstanceSaveResponse> saveOpportunity(
      Authentication authentication,
      @RequestBody HappenstanceSaveRequest request
  ) {
    return ResponseEntity.ok(happenstanceService.toggleSave(authentication.getName(), request.getOpportunityId()));
  }

  @PostMapping("/click")
  public ResponseEntity<java.util.Map<String, String>> recordClick(
      Authentication authentication,
      @RequestBody HappenstanceClickRequest request
  ) {
    happenstanceService.recordClick(authentication.getName(), request.getOpportunityId());
    return ResponseEntity.ok(java.util.Map.of("status", "ok"));
  }

  @GetMapping("/analytics")
  public HappenstanceAnalyticsResponse analytics(Authentication authentication) {
    return happenstanceService.getAnalytics(authentication.getName());
  }

  @GetMapping("/admin/analytics")
  public HappenstanceAdminAnalyticsResponse adminAnalytics(Authentication authentication) {
    return happenstanceService.getAdminAnalytics(authentication.getName());
  }

  @GetMapping("/interests")
  public List<String> interests(Authentication authentication) {
    return happenstanceService.getInterests(authentication.getName());
  }

  @GetMapping("/recommendations")
  public List<HappenstanceOpportunityResponse> recommendations(Authentication authentication) {
    return happenstanceService.getRecommendations(authentication.getName());
  }
}
