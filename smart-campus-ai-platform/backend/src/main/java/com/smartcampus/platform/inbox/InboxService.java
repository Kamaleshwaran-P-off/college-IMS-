package com.smartcampus.platform.inbox;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.inbox.dto.InboxBulkUpdateRequest;
import com.smartcampus.platform.inbox.dto.InboxEmailResponse;
import com.smartcampus.platform.inbox.dto.InboxEmailUpdateRequest;
import com.smartcampus.platform.defaultdata.RealisticDataGenerator;

@Service
@Transactional
public class InboxService {
  private final InboxEmailRepository inboxEmailRepository;
  private final EmailClassifier emailClassifier;
  private final RealisticDataGenerator dataGenerator;

  public InboxService(
      InboxEmailRepository inboxEmailRepository,
      EmailClassifier emailClassifier,
      RealisticDataGenerator dataGenerator
  ) {
    this.inboxEmailRepository = inboxEmailRepository;
    this.emailClassifier = emailClassifier;
    this.dataGenerator = dataGenerator;
  }

  public List<InboxEmailResponse> listEmails() {
    List<InboxEmailResponse> emails = inboxEmailRepository.findAll().stream()
        .sorted((a, b) -> b.getReceivedAt().compareTo(a.getReceivedAt()))
        .map(this::toResponse)
        .collect(Collectors.toList());
    if (!emails.isEmpty()) {
      return emails;
    }

    List<com.smartcampus.platform.gmail.dto.GmailEmailResponse> defaults = dataGenerator.getDefaultInbox();
    java.util.concurrent.atomic.AtomicLong seed = new java.util.concurrent.atomic.AtomicLong(-900L);
    return defaults.stream()
        .map(email -> {
          EmailCategory category = EmailCategory.GENERAL;
          if (email.getCategory() != null) {
            try {
              category = EmailCategory.valueOf(email.getCategory().toUpperCase());
            } catch (IllegalArgumentException ignored) {
              category = EmailCategory.GENERAL;
            }
          }
          boolean important = email.getImportant() != null ? email.getImportant()
              : category == EmailCategory.DEADLINES || category == EmailCategory.OFFICIAL;
          return new InboxEmailResponse(
              seed.getAndDecrement(),
              email.getSubject(),
              email.getSender(),
              email.getContent(),
              java.time.LocalDateTime.now(),
              category,
              category,
              important
          );
        })
        .toList();
  }

  public InboxEmailResponse updateEmail(Long id, InboxEmailUpdateRequest request) {
    InboxEmail email = inboxEmailRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email not found"));

    if (request.getCategory() != null) {
      email.setCategory(request.getCategory());
    }
    if (request.getImportant() != null) {
      email.setImportant(request.getImportant());
    }

    return toResponse(inboxEmailRepository.save(email));
  }

  public List<InboxEmailResponse> bulkUpdate(InboxBulkUpdateRequest request) {
    if (request.getIds() == null || request.getIds().isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No email ids provided");
    }

    List<InboxEmail> emails = inboxEmailRepository.findAllById(request.getIds());
    for (InboxEmail email : emails) {
      if (request.getCategory() != null) {
        email.setCategory(request.getCategory());
      }
      if (request.getImportant() != null) {
        email.setImportant(request.getImportant());
      }
    }
    return inboxEmailRepository.saveAll(emails).stream().map(this::toResponse).toList();
  }

  public void seedIfEmpty() {
    if (inboxEmailRepository.count() > 0) {
      return;
    }

    addSeed(
        "Circular: Semester Exam Schedule Updated",
        "office@campus.edu",
        "The revised semester exam schedule is now available. Please review the updated dates.",
        LocalDateTime.now().minusDays(1)
    );
    addSeed(
        "Hackathon: Campus AI Sprint 2026",
        "innovation@campus.edu",
        "Join the 24-hour AI sprint this April. Team registrations are open.",
        LocalDateTime.now().minusDays(2)
    );
    addSeed(
        "Library due reminder: Book return by Friday",
        "library@campus.edu",
        "Your borrowed book is due this Friday. Please return or renew to avoid fines.",
        LocalDateTime.now().minusDays(1)
    );
    addSeed(
        "Submission deadline: Mini-project reports",
        "faculty@campus.edu",
        "Submit your mini-project report by 30 March 2026.",
        LocalDateTime.now().minusDays(3)
    );
    addSeed(
        "Competition Alert: National Coding Challenge",
        "club@campus.edu",
        "Represent the campus in the upcoming national coding challenge.",
        LocalDateTime.now().minusDays(4)
    );
    addSeed(
        "Friendly reminder: Study group meetup",
        "student.affairs@campus.edu",
        "Study group meets this Thursday evening in the main hall.",
        LocalDateTime.now().minusDays(5)
    );
  }

  private void addSeed(String subject, String sender, String content, LocalDateTime receivedAt) {
    EmailCategory category = emailClassifier.classify(subject, content);
    boolean important = category == EmailCategory.DEADLINES || category == EmailCategory.OFFICIAL;
    InboxEmail email = new InboxEmail(subject, sender, content, receivedAt, category, important);
    inboxEmailRepository.save(email);
  }

  private InboxEmailResponse toResponse(InboxEmail email) {
    EmailCategory suggestion = emailClassifier.classify(email.getSubject(), email.getContent());
    return new InboxEmailResponse(
        email.getId(),
        email.getSubject(),
        email.getSender(),
        email.getContent(),
        email.getReceivedAt(),
        email.getCategory(),
        suggestion,
        email.isImportant()
    );
  }
}
