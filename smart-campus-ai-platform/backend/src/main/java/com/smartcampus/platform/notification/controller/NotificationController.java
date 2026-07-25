package com.smartcampus.platform.notification.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.smartcampus.platform.notification.dto.MarkReadRequest;
import com.smartcampus.platform.notification.dto.NotificationResponse;
import com.smartcampus.platform.notification.service.NotificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/system-notifications")
@Validated
public class NotificationController {
  private final NotificationService notificationService;

  public NotificationController(NotificationService notificationService) {
    this.notificationService = notificationService;
  }

  @GetMapping
  public List<NotificationResponse> latest(@RequestParam Long userId) {
    return notificationService.getLatest(userId);
  }

  @GetMapping("/unread-count")
  public long unreadCount(@RequestParam Long userId) {
    return notificationService.countUnread(userId);
  }

  @PostMapping("/mark-read")
  public ResponseEntity<Void> markRead(@Valid @RequestBody MarkReadRequest request) {
    notificationService.markRead(request.getId());
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }
}
