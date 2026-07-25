package com.smartcampus.platform.notification.broadcast.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.common.exception.ResourceNotFoundException;
import com.smartcampus.platform.notification.broadcast.dto.NotificationCreateRequest;
import com.smartcampus.platform.notification.broadcast.dto.NotificationResponse;
import com.smartcampus.platform.notification.broadcast.entity.BroadcastNotification;
import com.smartcampus.platform.notification.broadcast.entity.BroadcastNotificationRead;
import com.smartcampus.platform.notification.broadcast.entity.NotificationTargetRole;
import com.smartcampus.platform.notification.broadcast.repository.BroadcastNotificationReadRepository;
import com.smartcampus.platform.notification.broadcast.repository.BroadcastNotificationRepository;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.staff.service.StaffProfileService;
import com.smartcampus.platform.student.entity.Student;
import com.smartcampus.platform.student.service.StudentProfileService;

@Service
@Transactional
public class BroadcastNotificationService {
  private final BroadcastNotificationRepository broadcastNotificationRepository;
  private final BroadcastNotificationReadRepository broadcastNotificationReadRepository;
  private final UserRepository userRepository;
  private final StudentProfileService studentProfileService;
  private final StaffProfileService staffProfileService;

  public BroadcastNotificationService(
      BroadcastNotificationRepository broadcastNotificationRepository,
      BroadcastNotificationReadRepository broadcastNotificationReadRepository,
      UserRepository userRepository,
      StudentProfileService studentProfileService,
      StaffProfileService staffProfileService
  ) {
    this.broadcastNotificationRepository = broadcastNotificationRepository;
    this.broadcastNotificationReadRepository = broadcastNotificationReadRepository;
    this.userRepository = userRepository;
    this.studentProfileService = studentProfileService;
    this.staffProfileService = staffProfileService;
  }

  public List<NotificationResponse> create(NotificationCreateRequest request, String senderEmail) {
    User sender = userRepository.findByEmail(senderEmail)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    NotificationTargetRole targetRole = NotificationTargetRole.from(request.getTargetRole());
    if (targetRole == null) {
      throw new IllegalArgumentException("Target role is required");
    }

    if (sender.getRole() == Role.STAFF) {
      if (targetRole != NotificationTargetRole.STUDENT) {
        throw new IllegalArgumentException("Faculty can only send to students");
      }
      return createFacultyNotifications(request, sender, targetRole);
    }

    if (sender.getRole() != Role.ADMIN) {
      throw new IllegalArgumentException("Only ADMIN or FACULTY can send notifications");
    }

    BroadcastNotification notification = new BroadcastNotification(
        request.getTitle(),
        request.getMessage(),
        sender.getRole(),
        targetRole,
        normalize(request.getDepartment()),
        normalize(request.getClassName()),
        sender
    );

    return List.of(toResponse(broadcastNotificationRepository.save(notification), false));
  }

  public List<NotificationResponse> getForUser(String email) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    List<NotificationTargetRole> targets = new ArrayList<>();
    targets.add(NotificationTargetRole.ALL);
    if (user.getRole() == Role.STUDENT) {
      targets.add(NotificationTargetRole.STUDENT);
    } else if (user.getRole() == Role.STAFF) {
      targets.add(NotificationTargetRole.STAFF);
    } else if (user.getRole() == Role.ADMIN) {
      targets.add(NotificationTargetRole.STUDENT);
      targets.add(NotificationTargetRole.STAFF);
    }

    String department = null;
    String className = null;

    if (user.getRole() == Role.STUDENT) {
      Student student = studentProfileService.ensureForUser(user);
      department = normalize(student.getDepartment());
      className = normalize(buildClassKey(student.getDepartment(), student.getSection()));
    } else if (user.getRole() == Role.STAFF) {
      Staff staff = staffProfileService.ensureForUser(user);
      department = normalize(staff.getDepartment());
    }

    List<BroadcastNotification> notifications = broadcastNotificationRepository.findForUser(targets, department, className);
    if (notifications.isEmpty()) {
      return List.of();
    }

    List<Long> ids = notifications.stream().map(BroadcastNotification::getId).toList();
    Set<Long> readIds = broadcastNotificationReadRepository.findByUserIdAndNotificationIdIn(user.getId(), ids)
        .stream()
        .map(read -> read.getNotification().getId())
        .collect(Collectors.toSet());

    return notifications.stream()
        .map(notification -> toResponse(notification, readIds.contains(notification.getId())))
        .toList();
  }

  public void delete(Long id, String requesterEmail) {
    BroadcastNotification notification = broadcastNotificationRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
    User user = userRepository.findByEmail(requesterEmail)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (!notification.getCreatedBy().getId().equals(user.getId())) {
      throw new IllegalArgumentException("Only the sender can delete this notification");
    }
    broadcastNotificationRepository.delete(notification);
  }

  public void markRead(String requesterEmail, List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
      return;
    }
    User user = userRepository.findByEmail(requesterEmail)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    List<BroadcastNotification> notifications = broadcastNotificationRepository.findAllById(ids);
    for (BroadcastNotification notification : notifications) {
      if (!broadcastNotificationReadRepository.existsByUserIdAndNotificationId(user.getId(), notification.getId())) {
        broadcastNotificationReadRepository.save(new BroadcastNotificationRead(notification, user));
      }
    }
  }

  private List<NotificationResponse> createFacultyNotifications(
      NotificationCreateRequest request,
      User sender,
      NotificationTargetRole targetRole
  ) {
    Staff staff = staffProfileService.ensureForUser(sender);

    List<String> assignedClasses = splitClasses(staff.getAssignedClasses());
    if (assignedClasses.isEmpty()) {
      throw new IllegalArgumentException("Faculty has no assigned classes configured");
    }

    String className = normalize(request.getClassName());
    if (className == null) {
      List<NotificationResponse> responses = new ArrayList<>();
      for (String assigned : assignedClasses) {
        BroadcastNotification notification = new BroadcastNotification(
            request.getTitle(),
            request.getMessage(),
            sender.getRole(),
            targetRole,
            normalize(request.getDepartment()),
            assigned,
            sender
        );
        responses.add(toResponse(broadcastNotificationRepository.save(notification), false));
      }
      return responses;
    }

    if (!assignedClasses.contains(className)) {
      throw new IllegalArgumentException("Faculty can only send to assigned classes");
    }

    BroadcastNotification notification = new BroadcastNotification(
        request.getTitle(),
        request.getMessage(),
        sender.getRole(),
        targetRole,
        normalize(request.getDepartment()),
        className,
        sender
    );
    return List.of(toResponse(broadcastNotificationRepository.save(notification), false));
  }

  private List<String> splitClasses(String raw) {
    if (raw == null || raw.isBlank()) {
      return Collections.emptyList();
    }
    String[] parts = raw.split(",");
    List<String> results = new ArrayList<>();
    for (String part : parts) {
      String normalized = normalize(part);
      if (normalized != null) {
        results.add(normalized);
      }
    }
    return results;
  }

  private String buildClassKey(String department, String section) {
    if (department == null || section == null) {
      return null;
    }
    return department.trim() + "-" + section.trim();
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      return null;
    }
    return trimmed.toUpperCase(Locale.ROOT);
  }

  private NotificationResponse toResponse(BroadcastNotification notification, boolean read) {
    return new NotificationResponse(
        notification.getId(),
        notification.getTitle(),
        notification.getMessage(),
        notification.getSenderRole().name(),
        notification.getTargetRole().name(),
        notification.getDepartment(),
        notification.getClassName(),
        notification.getCreatedAt(),
        notification.getCreatedBy().getId(),
        notification.getCreatedBy().getFullName(),
        read
    );
  }
}
