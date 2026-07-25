package com.smartcampus.platform.emailintelligence.service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.smartcampus.platform.emailintelligence.EmailInsight;
import com.smartcampus.platform.emailintelligence.EmailInsightRepository;
import com.smartcampus.platform.emailintelligence.dto.EmailInsightPageResponse;
import com.smartcampus.platform.emailintelligence.dto.EmailInsightResponse;
import com.smartcampus.platform.gmail.entity.Email;
import com.smartcampus.platform.gmail.repository.EmailRepository;

@Service
public class EmailIntelligenceService {
  private static final int MAX_LIMIT = 100;
  private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private final EmailRepository emailRepository;
  private final EmailInsightRepository insightRepository;
  private final AiEmailService aiEmailService;
  private final com.smartcampus.platform.emailintelligence.task.EmailTaskService taskService;
  private final com.smartcampus.platform.emailintelligence.hackathon.EmailHackathonService hackathonService;

  public EmailIntelligenceService(
      EmailRepository emailRepository,
      EmailInsightRepository insightRepository,
      AiEmailService aiEmailService,
      com.smartcampus.platform.emailintelligence.task.EmailTaskService taskService,
      com.smartcampus.platform.emailintelligence.hackathon.EmailHackathonService hackathonService
  ) {
    this.emailRepository = emailRepository;
    this.insightRepository = insightRepository;
    this.aiEmailService = aiEmailService;
    this.taskService = taskService;
    this.hackathonService = hackathonService;
  }

  public List<EmailInsightResponse> processLatest(Long userId, int limit) {
    int safeLimit = Math.min(Math.max(limit, 1), MAX_LIMIT);
    Pageable pageable = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Email> emails = emailRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    return emails.getContent().stream()
        .map(email -> {
          EmailInsight insight = aiEmailService.processEmail(email);
          taskService.createFromInsight(insight);
          hackathonService.createIfMatches(email, insight);
          return insight;
        })
        .filter(Objects::nonNull)
        .map(this::toResponse)
        .toList();
  }

  public List<EmailInsightResponse> getInsights(Long userId, int page, int size) {
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), MAX_LIMIT);
    Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<EmailInsight> insights = insightRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    return insights.getContent().stream()
        .map(this::toResponse)
        .toList();
  }

  public EmailInsightPageResponse getInsightsPage(Long userId, int page, int size) {
    int safePage = Math.max(page, 0);
    int safeSize = Math.min(Math.max(size, 1), MAX_LIMIT);
    Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<EmailInsight> insights = insightRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    List<EmailInsightResponse> items = insights.getContent().stream()
        .map(this::toResponse)
        .toList();
    return new EmailInsightPageResponse(
        items,
        insights.getNumber(),
        insights.getSize(),
        insights.getTotalElements(),
        insights.getTotalPages()
    );
  }

  private EmailInsightResponse toResponse(EmailInsight insight) {
    Email email = insight.getEmail();
    String createdAt = insight.getCreatedAt() != null ? DATE_TIME.format(insight.getCreatedAt()) : "";
    String deadline = insight.getDeadline() != null ? insight.getDeadline().toString() : null;
    return new EmailInsightResponse(
        insight.getId(),
        email.getId(),
        email.getMessageId(),
        email.getSubject(),
        email.getSender(),
        insight.getSummary(),
        insight.getCategory(),
        deadline,
        insight.getPriority().name(),
        insight.isActionRequired(),
        createdAt
    );
  }
}
