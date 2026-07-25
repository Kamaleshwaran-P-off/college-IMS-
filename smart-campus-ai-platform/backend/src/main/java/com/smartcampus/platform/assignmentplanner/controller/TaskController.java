package com.smartcampus.platform.assignmentplanner.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.assignmentplanner.dto.ScheduledTaskResponse;
import com.smartcampus.platform.assignmentplanner.service.SmartSchedulerService;
import com.smartcampus.platform.auth.repository.UserRepository;

@RestController
@RequestMapping("/api/task")
public class TaskController {
  private final SmartSchedulerService schedulerService;
  private final UserRepository userRepository;

  public TaskController(SmartSchedulerService schedulerService, UserRepository userRepository) {
    this.schedulerService = schedulerService;
    this.userRepository = userRepository;
  }

  @PatchMapping("/{id}/complete")
  public ScheduledTaskResponse complete(
      Authentication authentication,
      @PathVariable Long id
  ) {
    var user = userRepository.findByEmail(authentication.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    return schedulerService.markCompleted(user, id);
  }
}
