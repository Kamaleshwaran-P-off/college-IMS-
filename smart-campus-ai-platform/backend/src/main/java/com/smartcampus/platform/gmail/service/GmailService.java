package com.smartcampus.platform.gmail.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.smartcampus.platform.gmail.dto.GmailEmailResponse;

/**
 * Thin facade for Gmail Phase 1 integration.
 * Keeps existing OAuth + inbox logic untouched while exposing a clean service API.
 */
@Service
public class GmailService {
  private final GmailInboxService inboxService;
  private final GmailOAuthService oauthService;

  public GmailService(GmailInboxService inboxService, GmailOAuthService oauthService) {
    this.inboxService = inboxService;
    this.oauthService = oauthService;
  }

  public List<GmailEmailResponse> fetchEmails(Long userId) {
    return inboxService.fetchEmails(userId);
  }

  public String buildAuthUrl(Long userId) {
    return oauthService.buildAuthUrl(userId);
  }
}
