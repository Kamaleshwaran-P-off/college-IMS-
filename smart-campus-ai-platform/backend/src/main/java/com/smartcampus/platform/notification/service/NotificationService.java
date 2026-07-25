package com.smartcampus.platform.notification.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartcampus.platform.assignment.entity.Assignment;
import com.smartcampus.platform.assignment.repository.AssignmentRepository;
import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.common.exception.ResourceNotFoundException;
import com.smartcampus.platform.notification.dto.NotificationResponse;
import com.smartcampus.platform.notification.entity.Notification;
import com.smartcampus.platform.notification.entity.NotificationType;
import com.smartcampus.platform.notification.repository.NotificationRepository;

@Service
@Transactional
public class NotificationService {
  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;
  private final AssignmentRepository assignmentRepository;
  private final SimpMessagingTemplate messagingTemplate;
  private final int deadlineWindowDays;

  public NotificationService(
      NotificationRepository notificationRepository,
      UserRepository userRepository,
      AssignmentRepository assignmentRepository,
      SimpMessagingTemplate messagingTemplate,
      @Value("${app.notifications.deadline-window-days:2}") int deadlineWindowDays
  ) {
    this.notificationRepository = notificationRepository;
    this.userRepository = userRepository;
    this.assignmentRepository = assignmentRepository;
    this.messagingTemplate = messagingTemplate;
    this.deadlineWindowDays = deadlineWindowDays;
  }

  public NotificationResponse createNotification(Long userId, NotificationType type, String title, String message) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    Notification notification = new Notification(user, type, title, message);
    notificationRepository.save(notification);

    NotificationResponse response = toResponse(notification);
    messagingTemplate.convertAndSend("/topic/notifications/" + userId, response);
    return response;
  }

  public List<NotificationResponse> getLatest(Long userId) {
    return notificationRepository.findTop20ByUserIdOrderByCreatedAtDesc(userId)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  public long countUnread(Long userId) {
    return notificationRepository.countByUserIdAndReadFalse(userId);
  }

  public void markRead(Long id) {
    Notification notification = notificationRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
    notification.setRead(true);
  }

  @Scheduled(cron = "0 0 9 * * *")
  public void sendDeadlineReminders() {
    LocalDate today = LocalDate.now();
    LocalDate deadlineLimit = today.plusDays(deadlineWindowDays);

    List<Assignment> assignments = assignmentRepository.findAll();
    for (Assignment assignment : assignments) {
      if (assignment.getDueDate() == null) {
        continue;
      }

      LocalDate due = assignment.getDueDate();
      if (due.isBefore(today) || due.isAfter(deadlineLimit)) {
        continue;
      }

      Long studentUserId = assignment.getAssignedStudent().getUser().getId();
      String title = "Assignment due soon";
      String message = assignment.getTitle() + " is due on " + due + ".";
      createNotification(studentUserId, NotificationType.DEADLINE, title, message);
    }
  }

  private NotificationResponse toResponse(Notification notification) {
    return new NotificationResponse(
        notification.getId(),
        notification.getType(),
        notification.getTitle(),
        notification.getMessage(),
        notification.isRead(),
        notification.getCreatedAt()
    );
  }
}
