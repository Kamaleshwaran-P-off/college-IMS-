package com.smartcampus.platform.gmail.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.smartcampus.platform.gmail.dto.EmailPageResponse;
import com.smartcampus.platform.gmail.dto.GmailEmailResponse;
import com.smartcampus.platform.gmail.dto.StoredEmailResponse;
import com.smartcampus.platform.gmail.entity.Email;
import com.smartcampus.platform.gmail.repository.EmailRepository;

@Service
public class GmailEmailStorageService {
  private static final int MAX_PAGE_SIZE = 100;
  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private final EmailRepository emailRepository;

  public GmailEmailStorageService(EmailRepository emailRepository) {
    this.emailRepository = emailRepository;
  }

  public List<StoredEmailResponse> syncEmails(Long userId, List<GmailEmailResponse> emails) {
    return emails.stream()
        .map(email -> upsertEmail(userId, email))
        .map(this::toResponse)
        .toList();
  }

  public EmailPageResponse getStoredEmails(Long userId, int page, int size) {
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
    Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Email> result = emailRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    List<StoredEmailResponse> items = result.getContent().stream()
        .map(this::toResponse)
        .toList();
    return new EmailPageResponse(items, result.getNumber(), result.getSize(), result.getTotalElements(), result.getTotalPages());
  }

  private Email upsertEmail(Long userId, GmailEmailResponse email) {
    Email entity = emailRepository.findByUserIdAndMessageId(userId, email.getId())
        .orElse(new Email());
    entity.setUserId(userId);
    entity.setMessageId(email.getId());
    entity.setSubject(safeText(email.getSubject(), "(No subject)"));
    entity.setSender(safeText(email.getSender(), "Unknown sender"));
    entity.setBody(safeText(email.getContent(), ""));
    if (entity.getCreatedAt() == null) {
      entity.setCreatedAt(LocalDateTime.now());
    }
    return emailRepository.save(entity);
  }

  private StoredEmailResponse toResponse(Email email) {
    String createdAt = email.getCreatedAt() != null ? FORMATTER.format(email.getCreatedAt()) : "";
    return new StoredEmailResponse(
        email.getId(),
        email.getMessageId(),
        email.getSubject(),
        email.getSender(),
        email.getBody(),
        createdAt
    );
  }

  private String safeText(String value, String fallback) {
    if (value == null) return fallback;
    String trimmed = value.trim();
    return trimmed.isEmpty() ? fallback : trimmed;
  }
}
