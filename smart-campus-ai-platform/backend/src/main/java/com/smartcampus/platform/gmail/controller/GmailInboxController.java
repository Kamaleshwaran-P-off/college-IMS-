package com.smartcampus.platform.gmail.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.gmail.dto.GmailBulkUpdateRequest;
import com.smartcampus.platform.gmail.dto.GmailEmailResponse;
import com.smartcampus.platform.gmail.dto.GmailUpdateRequest;
import com.smartcampus.platform.gmail.service.GmailInboxService;

@RestController
@RequestMapping("/api/emails")
public class GmailInboxController {
  private final GmailInboxService inboxService;
  private final UserRepository userRepository;

  public GmailInboxController(GmailInboxService inboxService, UserRepository userRepository) {
    this.inboxService = inboxService;
    this.userRepository = userRepository;
  }

  @GetMapping
  public List<GmailEmailResponse> getEmails(Authentication authentication) {
    Long userId = getUserId(authentication);
    return inboxService.fetchEmails(userId);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<GmailEmailResponse> updateEmail(
      Authentication authentication,
      @PathVariable String id,
      @RequestBody GmailUpdateRequest request
  ) {
    Long userId = getUserId(authentication);
    return ResponseEntity.ok(inboxService.updateEmail(userId, id, request));
  }

  @PatchMapping("/bulk")
  public ResponseEntity<List<GmailEmailResponse>> bulkUpdate(
      Authentication authentication,
      @RequestBody GmailBulkUpdateRequest request
  ) {
    Long userId = getUserId(authentication);
    return ResponseEntity.ok(inboxService.bulkUpdate(userId, request));
  }

  private Long getUserId(Authentication authentication) {
    String email = authentication.getName();
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    return user.getId();
  }
}
