package com.smartcampus.platform.gmail.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.gmail.dto.GmailBulkUpdateRequest;
import com.smartcampus.platform.gmail.dto.GmailEmailResponse;
import com.smartcampus.platform.gmail.dto.GmailUpdateRequest;
import com.smartcampus.platform.gmail.entity.GmailOverride;
import com.smartcampus.platform.gmail.entity.GmailToken;
import com.smartcampus.platform.gmail.repository.GmailOverrideRepository;
import com.smartcampus.platform.gmail.repository.GmailTokenRepository;
import com.smartcampus.platform.defaultdata.RealisticDataGenerator;

@Service
public class GmailInboxService {
  private final GmailTokenRepository tokenRepository;
  private final GmailOverrideRepository overrideRepository;
  private final GmailOAuthService oauthService;
  private final GmailApiService apiService;
  private final RealisticDataGenerator dataGenerator;

  public GmailInboxService(
      GmailTokenRepository tokenRepository,
      GmailOverrideRepository overrideRepository,
      GmailOAuthService oauthService,
      GmailApiService apiService,
      RealisticDataGenerator dataGenerator
  ) {
    this.tokenRepository = tokenRepository;
    this.overrideRepository = overrideRepository;
    this.oauthService = oauthService;
    this.apiService = apiService;
    this.dataGenerator = dataGenerator;
  }

  public List<GmailEmailResponse> fetchEmails(Long userId) {
    try {
      GmailToken token = tokenRepository.findByUserId(userId)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED, "GMAIL_NOT_LINKED"));

      token = oauthService.refreshTokenIfNeeded(token);

      List<GmailEmailResponse> emails = apiService.fetchEmails(token.getAccessToken(), 25);
      Map<String, GmailOverride> overrides = overrideRepository.findByUserId(userId)
          .stream()
          .collect(Collectors.toMap(GmailOverride::getMessageId, Function.identity()));

      for (GmailEmailResponse email : emails) {
        GmailOverride override = overrides.get(email.getId());
        if (override != null) {
          email.setCategory(override.getCategory());
          email.setImportant(override.getImportant());
        }
      }

      if (emails.isEmpty()) {
        return dataGenerator.getDefaultInbox();
      }

      return emails;
    } catch (Exception ex) {
      return dataGenerator.getDefaultInbox();
    }
  }

  public GmailEmailResponse updateEmail(Long userId, String messageId, GmailUpdateRequest request) {
    GmailOverride override = overrideRepository.findByUserIdAndMessageId(userId, messageId)
        .orElse(new GmailOverride(userId, messageId, null, null, LocalDateTime.now()));

    if (request.getCategory() != null) {
      override.setCategory(request.getCategory());
    }
    if (request.getImportant() != null) {
      override.setImportant(request.getImportant());
    }
    override.setUpdatedAt(LocalDateTime.now());
    overrideRepository.save(override);

    GmailEmailResponse response = new GmailEmailResponse();
    response.setId(messageId);
    response.setCategory(override.getCategory());
    response.setImportant(override.getImportant());
    return response;
  }

  public List<GmailEmailResponse> bulkUpdate(Long userId, GmailBulkUpdateRequest request) {
    if (request.getIds() == null || request.getIds().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No message IDs provided");
    }
    return request.getIds().stream()
        .map(id -> {
          GmailUpdateRequest update = new GmailUpdateRequest();
          update.setCategory(request.getCategory());
          update.setImportant(request.getImportant());
          return updateEmail(userId, id, update);
        })
        .toList();
  }
}
