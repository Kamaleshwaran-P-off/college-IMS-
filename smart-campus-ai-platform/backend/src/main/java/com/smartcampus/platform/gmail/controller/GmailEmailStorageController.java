package com.smartcampus.platform.gmail.controller;

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
import com.smartcampus.platform.gmail.dto.EmailPageResponse;
import com.smartcampus.platform.gmail.dto.GmailEmailResponse;
import com.smartcampus.platform.gmail.dto.StoredEmailResponse;
import com.smartcampus.platform.gmail.service.GmailEmailStorageService;
import com.smartcampus.platform.gmail.service.GmailService;

/**
 * Phase 2: Store Gmail emails and expose paginated access.
 */
@RestController
@RequestMapping("/api/gmail/emails")
public class GmailEmailStorageController {
  private final GmailService gmailService;
  private final GmailEmailStorageService storageService;
  private final UserRepository userRepository;

  public GmailEmailStorageController(
      GmailService gmailService,
      GmailEmailStorageService storageService,
      UserRepository userRepository
  ) {
    this.gmailService = gmailService;
    this.storageService = storageService;
    this.userRepository = userRepository;
  }

  @PostMapping("/sync")
  public List<StoredEmailResponse> syncEmails(Authentication authentication,
      @RequestParam(value = "maxResults", defaultValue = "25") int maxResults
  ) {
    Long userId = getUserId(authentication);
    List<GmailEmailResponse> emails = gmailService.fetchEmails(userId);
    int safeMax = Math.max(maxResults, 1);
    List<GmailEmailResponse> limited = emails.stream().limit(safeMax).toList();
    return storageService.syncEmails(userId, limited);
  }

  @GetMapping("/stored")
  public EmailPageResponse getStoredEmails(
      Authentication authentication,
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "20") int size
  ) {
    Long userId = getUserId(authentication);
    return storageService.getStoredEmails(userId, page, size);
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
