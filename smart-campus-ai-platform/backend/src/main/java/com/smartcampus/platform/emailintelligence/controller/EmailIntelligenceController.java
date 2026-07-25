package com.smartcampus.platform.emailintelligence.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.emailintelligence.dto.EmailInsightPageResponse;
import com.smartcampus.platform.emailintelligence.dto.EmailInsightResponse;
import com.smartcampus.platform.emailintelligence.service.EmailIntelligenceService;

@RestController
@RequestMapping("/api/email-intelligence")
public class EmailIntelligenceController {
  private final EmailIntelligenceService intelligenceService;
  private final UserRepository userRepository;

  public EmailIntelligenceController(
      EmailIntelligenceService intelligenceService,
      UserRepository userRepository
  ) {
    this.intelligenceService = intelligenceService;
    this.userRepository = userRepository;
  }

  @PostMapping("/process")
  public List<EmailInsightResponse> process(
      Authentication authentication,
      @RequestParam(value = "limit", defaultValue = "20") int limit
  ) {
    Long userId = getUserId(authentication);
    return intelligenceService.processLatest(userId, limit);
  }

  @GetMapping
  public List<EmailInsightResponse> getInsights(
      Authentication authentication,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "20") int size
  ) {
    Long userId = getUserId(authentication);
    return intelligenceService.getInsights(userId, page, size);
  }

  @GetMapping("/page")
  public EmailInsightPageResponse getInsightsPage(
      Authentication authentication,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "20") int size
  ) {
    Long userId = getUserId(authentication);
    return intelligenceService.getInsightsPage(userId, page, size);
  }

  private Long getUserId(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authentication");
    }
    String email = authentication.getName();
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    return user.getId();
  }
}
