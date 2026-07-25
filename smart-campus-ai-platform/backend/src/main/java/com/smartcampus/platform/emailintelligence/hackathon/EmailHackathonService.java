package com.smartcampus.platform.emailintelligence.hackathon;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;

import com.smartcampus.platform.emailintelligence.EmailInsight;
import com.smartcampus.platform.emailintelligence.hackathon.dto.EmailHackathonResponse;
import com.smartcampus.platform.gmail.entity.Email;

@Service
public class EmailHackathonService {
  private static final List<String> KEYWORDS = List.of(
      "hackathon",
      "unstop",
      "devfolio",
      "hackerearth",
      "codefest",
      "buildathon"
  );
  private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private final EmailHackathonRepository repository;

  public EmailHackathonService(EmailHackathonRepository repository) {
    this.repository = repository;
  }

  public EmailHackathon createIfMatches(Email email, EmailInsight insight) {
    if (email == null) {
      return null;
    }
    if (!matches(email)) {
      return null;
    }
    return repository.findByEmailId(email.getId())
        .orElseGet(() -> {
          EmailHackathon hackathon = new EmailHackathon();
          hackathon.setEmail(email);
          hackathon.setUserId(email.getUserId());
          hackathon.setName(deriveName(email.getSubject()));
          LocalDate deadline = insight != null ? insight.getDeadline() : null;
          hackathon.setDeadline(deadline);
          hackathon.setCreatedAt(LocalDateTime.now());
          return repository.save(hackathon);
        });
  }

  public List<EmailHackathonResponse> getHackathons(Long userId) {
    return repository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(this::toResponse)
        .toList();
  }

  private boolean matches(Email email) {
    String subject = normalize(email.getSubject());
    String body = normalize(email.getBody());
    String combined = subject + " " + body;
    for (String keyword : KEYWORDS) {
      if (combined.contains(keyword)) {
        return true;
      }
    }
    return false;
  }

  private String normalize(String value) {
    if (value == null) {
      return "";
    }
    return value.toLowerCase(Locale.ENGLISH);
  }

  private String deriveName(String subject) {
    if (subject == null || subject.isBlank()) {
      return "Hackathon Opportunity";
    }
    String cleaned = subject.replaceFirst("(?i)^hackathon[:\\s-]*", "").trim();
    return cleaned.isBlank() ? "Hackathon Opportunity" : cleaned;
  }

  private EmailHackathonResponse toResponse(EmailHackathon hackathon) {
    String createdAt = hackathon.getCreatedAt() != null ? DATE_TIME.format(hackathon.getCreatedAt()) : "";
    String deadline = hackathon.getDeadline() != null ? hackathon.getDeadline().toString() : null;
    Email email = hackathon.getEmail();
    return new EmailHackathonResponse(
        hackathon.getId(),
        email.getId(),
        email.getMessageId(),
        hackathon.getName(),
        email.getSubject(),
        email.getSender(),
        deadline,
        createdAt
    );
  }
}
