package com.smartcampus.platform.planner.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartcampus.platform.planner.entity.StudyTask;
import com.smartcampus.platform.planner.repository.StudyTaskRepository;

@Service
public class StudyReminderService {
  private static final Logger log = LoggerFactory.getLogger(StudyReminderService.class);

  private final StudyTaskRepository taskRepository;
  private final JavaMailSender mailSender;
  private final String mailFrom;
  private final String mailUsername;

  public StudyReminderService(
      StudyTaskRepository taskRepository,
      JavaMailSender mailSender,
      @Value("${app.mail.from:}") String mailFrom,
      @Value("${spring.mail.username:}") String mailUsername
  ) {
    this.taskRepository = taskRepository;
    this.mailSender = mailSender;
    this.mailFrom = mailFrom;
    this.mailUsername = mailUsername;
  }

  @Scheduled(fixedDelayString = "${app.reminders.interval-ms:60000}")
  @Transactional
  public void sendReminders() {
    if (mailUsername == null || mailUsername.isBlank()) {
      return;
    }

    LocalDateTime now = LocalDateTime.now();
    List<StudyTask> dueTasks = taskRepository
        .findByReminderAtNotNullAndReminderAtLessThanEqualAndReminderSentFalseAndCompletedFalse(now);

    if (dueTasks.isEmpty()) {
      return;
    }

    for (StudyTask task : dueTasks) {
      String to = task.getPlan().getUser().getEmail();
      if (to == null || to.isBlank()) {
        continue;
      }

      String subject = "Study Reminder: " + task.getTitle();
      String body = buildBody(task);

      try {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        if (mailFrom != null && !mailFrom.isBlank()) {
          message.setFrom(mailFrom);
        }
        mailSender.send(message);
        task.setReminderSent(true);
      } catch (Exception ex) {
        log.warn("Failed to send reminder for task {}: {}", task.getId(), ex.getMessage());
      }
    }
  }

  private String buildBody(StudyTask task) {
    StringBuilder sb = new StringBuilder();
    sb.append("Hi ").append(task.getPlan().getUser().getFullName()).append(",\n\n");
    sb.append("Reminder for your study plan task:\n");
    sb.append("Day: ").append(task.getDayLabel()).append("\n");
    sb.append("Task: ").append(task.getTitle()).append("\n");
    if (task.getDetails() != null && !task.getDetails().isBlank()) {
      sb.append("Details: ").append(task.getDetails()).append("\n");
    }
    sb.append("Week starting: ").append(task.getPlan().getWeekStart()).append("\n\n");
    sb.append("Stay focused!\n");
    sb.append("Smart Campus AI Platform");
    return sb.toString();
  }
}
