package com.smartcampus.platform.emailintelligence.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.platform.chat.service.GeminiService;
import com.smartcampus.platform.emailintelligence.EmailInsight;
import com.smartcampus.platform.emailintelligence.EmailInsightRepository;
import com.smartcampus.platform.emailintelligence.EmailPriority;
import com.smartcampus.platform.gmail.entity.Email;

@Service
public class AiEmailService {
  private static final Logger log = LoggerFactory.getLogger(AiEmailService.class);
  private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

  private final GeminiService geminiService;
  private final ObjectMapper objectMapper;
  private final EmailInsightRepository insightRepository;

  public AiEmailService(
      GeminiService geminiService,
      ObjectMapper objectMapper,
      EmailInsightRepository insightRepository
  ) {
    this.geminiService = geminiService;
    this.objectMapper = objectMapper;
    this.insightRepository = insightRepository;
  }

  public EmailInsight processEmail(Email email) {
    return insightRepository.findByEmailId(email.getId())
        .orElseGet(() -> generateInsight(email));
  }

  private EmailInsight generateInsight(Email email) {
    try {
      String response = geminiService.generateStructuredResponse(buildSystemPrompt(), buildUserPrompt(email));
      EmailInsightPayload payload = parsePayload(response);
      if (payload == null) {
        return null;
      }

      EmailInsight insight = new EmailInsight();
      insight.setEmail(email);
      insight.setUserId(email.getUserId());
      insight.setCategory(payload.category());
      insight.setSummary(payload.summary());
      insight.setDeadline(payload.deadline());
      insight.setPriority(payload.priority());
      insight.setActionRequired(payload.actionRequired());
      LocalDateTime now = LocalDateTime.now();
      insight.setCreatedAt(now);
      insight.setUpdatedAt(now);
      return insightRepository.save(insight);
    } catch (Exception ex) {
      log.warn("AI email processing failed for email {}", email.getId(), ex);
      return null;
    }
  }

  private EmailInsightPayload parsePayload(String response) {
    if (response == null || response.isBlank()) {
      return null;
    }
    String cleaned = stripJson(response);
    try {
      JsonNode node = objectMapper.readTree(cleaned);
      String category = node.path("category").asText("General");
      String summary = node.path("summary").asText("");
      String deadlineRaw = node.path("deadline").asText("");
      LocalDate deadline = parseDeadline(deadlineRaw);
      String priorityRaw = node.path("priority").asText("MEDIUM");
      EmailPriority priority = parsePriority(priorityRaw);
      boolean actionRequired = node.path("action_required").asBoolean(node.path("actionRequired").asBoolean(false));
      if (summary.isBlank()) {
        summary = "No summary available.";
      }
      return new EmailInsightPayload(category, summary, deadline, priority, actionRequired);
    } catch (Exception ex) {
      log.warn("Failed to parse AI response: {}", response, ex);
      return null;
    }
  }

  private EmailPriority parsePriority(String value) {
    if (value == null) {
      return EmailPriority.MEDIUM;
    }
    String normalized = value.trim().toUpperCase();
    return switch (normalized) {
      case "HIGH" -> EmailPriority.HIGH;
      case "LOW" -> EmailPriority.LOW;
      default -> EmailPriority.MEDIUM;
    };
  }

  private LocalDate parseDeadline(String value) {
    if (value == null || value.isBlank() || "null".equalsIgnoreCase(value.trim())) {
      return null;
    }
    try {
      return LocalDate.parse(value.trim(), DATE_FORMAT);
    } catch (Exception ex) {
      return null;
    }
  }

  private String stripJson(String response) {
    String trimmed = response.trim();
    if (trimmed.startsWith("```")) {
      trimmed = trimmed.replaceAll("^```[a-zA-Z]*", "");
      trimmed = trimmed.replaceAll("```$", "");
    }
    int start = trimmed.indexOf('{');
    int end = trimmed.lastIndexOf('}');
    if (start >= 0 && end > start) {
      return trimmed.substring(start, end + 1);
    }
    return trimmed;
  }

  private String buildSystemPrompt() {
    return """
        You are an assistant that extracts structured insights from a single email.
        Return ONLY valid JSON with keys:
        category, summary, deadline, priority, action_required.
        deadline must be in YYYY-MM-DD or null.
        priority must be LOW, MEDIUM, or HIGH.
        """;
  }

  private String buildUserPrompt(Email email) {
    return """
        Email subject: %s
        Sender: %s
        Body: %s
        """.formatted(
        safe(email.getSubject()),
        safe(email.getSender()),
        safe(email.getBody())
    );
  }

  private String safe(String value) {
    return value == null ? "" : value.trim();
  }

  private record EmailInsightPayload(
      String category,
      String summary,
      LocalDate deadline,
      EmailPriority priority,
      boolean actionRequired
  ) {}
}
