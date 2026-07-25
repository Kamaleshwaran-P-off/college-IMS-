package com.smartcampus.platform.emailintelligence.task;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.emailintelligence.EmailInsight;
import com.smartcampus.platform.emailintelligence.EmailPriority;
import com.smartcampus.platform.emailintelligence.task.dto.EmailTaskResponse;
import com.smartcampus.platform.gmail.entity.Email;

@Service
public class EmailTaskService {
  private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  private final EmailTaskRepository taskRepository;

  public EmailTaskService(EmailTaskRepository taskRepository) {
    this.taskRepository = taskRepository;
  }

  public EmailTask createFromInsight(EmailInsight insight) {
    if (insight == null || !insight.isActionRequired()) {
      return null;
    }
    Email email = insight.getEmail();
    if (email == null) {
      return null;
    }
    return taskRepository.findByEmailId(email.getId())
        .orElseGet(() -> {
          EmailTask task = new EmailTask();
          task.setEmail(email);
          task.setUserId(insight.getUserId());
          task.setTitle(buildTitle(email.getSubject(), insight.getCategory()));
          task.setDeadline(insight.getDeadline());
          task.setPriority(insight.getPriority() != null ? insight.getPriority() : EmailPriority.MEDIUM);
          task.setCompleted(false);
          task.setCreatedAt(LocalDateTime.now());
          return taskRepository.save(task);
        });
  }

  public List<EmailTaskResponse> getTasks(Long userId) {
    return taskRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
        .map(this::toResponse)
        .toList();
  }

  public EmailTaskResponse markCompleted(Long userId, Long taskId, boolean completed) {
    EmailTask task = taskRepository.findByIdAndUserId(taskId, userId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    task.setCompleted(completed);
    return toResponse(taskRepository.save(task));
  }

  private EmailTaskResponse toResponse(EmailTask task) {
    String createdAt = task.getCreatedAt() != null ? DATE_TIME.format(task.getCreatedAt()) : "";
    String deadline = task.getDeadline() != null ? task.getDeadline().toString() : null;
    return new EmailTaskResponse(
        task.getId(),
        task.getEmail().getId(),
        task.getEmail().getSubject(),
        task.getTitle(),
        deadline,
        task.getPriority() != null ? task.getPriority().name() : EmailPriority.MEDIUM.name(),
        task.isCompleted(),
        createdAt
    );
  }

  private String buildTitle(String subject, String category) {
    String cleanSubject = subject == null || subject.isBlank() ? "Follow up on recent email" : subject.trim();
    if (category == null || category.isBlank()) {
      return "Action: " + cleanSubject;
    }
    return "Action (" + category.trim() + "): " + cleanSubject;
  }
}
