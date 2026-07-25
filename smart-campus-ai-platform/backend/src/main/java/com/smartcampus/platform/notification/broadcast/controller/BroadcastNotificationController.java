package com.smartcampus.platform.notification.broadcast.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.smartcampus.platform.notification.broadcast.dto.BroadcastMarkReadRequest;
import com.smartcampus.platform.notification.broadcast.dto.NotificationCreateRequest;
import com.smartcampus.platform.notification.broadcast.dto.NotificationResponse;
import com.smartcampus.platform.notification.broadcast.service.BroadcastNotificationService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/notifications")
@Validated
public class BroadcastNotificationController {
  private final BroadcastNotificationService broadcastNotificationService;

  public BroadcastNotificationController(BroadcastNotificationService broadcastNotificationService) {
    this.broadcastNotificationService = broadcastNotificationService;
  }

  @PostMapping
  public ResponseEntity<List<NotificationResponse>> create(
      @Valid @RequestBody NotificationCreateRequest request,
      Authentication authentication
  ) {
    String email = authentication.getName();
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(broadcastNotificationService.create(request, email));
  }

  @GetMapping
  public List<NotificationResponse> list(Authentication authentication) {
    String email = authentication.getName();
    return broadcastNotificationService.getForUser(email);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
    String email = authentication.getName();
    broadcastNotificationService.delete(id, email);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/mark-read")
  public ResponseEntity<Void> markRead(
      @Valid @RequestBody BroadcastMarkReadRequest request,
      Authentication authentication
  ) {
    String email = authentication.getName();
    broadcastNotificationService.markRead(email, request.getIds());
    return ResponseEntity.noContent().build();
  }
}
