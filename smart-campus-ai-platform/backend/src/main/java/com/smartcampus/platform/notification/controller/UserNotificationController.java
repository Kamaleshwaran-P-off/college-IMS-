package com.smartcampus.platform.notification.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.common.exception.ResourceNotFoundException;
import com.smartcampus.platform.notification.dto.MarkReadRequest;
import com.smartcampus.platform.notification.dto.NotificationResponse;
import com.smartcampus.platform.notification.service.NotificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications/user")
@Validated
public class UserNotificationController {
  private final NotificationService notificationService;
  private final UserRepository userRepository;

  public UserNotificationController(NotificationService notificationService, UserRepository userRepository) {
    this.notificationService = notificationService;
    this.userRepository = userRepository;
  }

  @GetMapping
  public List<NotificationResponse> getForUser(Authentication authentication) {
    var user = userRepository.findByEmail(authentication.getName())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return notificationService.getLatest(user.getId());
  }

  @GetMapping("/unread-count")
  public long unreadCount(Authentication authentication) {
    var user = userRepository.findByEmail(authentication.getName())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    return notificationService.countUnread(user.getId());
  }

  @PostMapping("/mark-read")
  public ResponseEntity<Void> markRead(@Valid @RequestBody MarkReadRequest request) {
    notificationService.markRead(request.getId());
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
