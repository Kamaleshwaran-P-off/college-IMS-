package com.smartcampus.platform.gmail.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.gmail.dto.GmailEmailResponse;
import com.smartcampus.platform.gmail.service.GmailService;

/**
 * Phase 1 Gmail API: fetch emails for the connected account.
 * Uses existing auth (JWT) and Gmail token storage without changing current endpoints.
 */
@RestController
@RequestMapping("/api/gmail/emails")
public class GmailEmailsController {
  private final GmailService gmailService;
  private final UserRepository userRepository;

  public GmailEmailsController(GmailService gmailService, UserRepository userRepository) {
    this.gmailService = gmailService;
    this.userRepository = userRepository;
  }

  @GetMapping
  public List<GmailEmailResponse> getEmails(Authentication authentication) {
    Long userId = getUserId(authentication);
    return gmailService.fetchEmails(userId);
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
