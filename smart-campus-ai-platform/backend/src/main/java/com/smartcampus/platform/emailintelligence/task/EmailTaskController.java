package com.smartcampus.platform.emailintelligence.task;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.emailintelligence.task.dto.EmailTaskResponse;

@RestController
@RequestMapping("/api/email-intelligence/tasks")
public class EmailTaskController {
  private final EmailTaskService taskService;
  private final UserRepository userRepository;

  public EmailTaskController(EmailTaskService taskService, UserRepository userRepository) {
    this.taskService = taskService;
    this.userRepository = userRepository;
  }

  @GetMapping
  public List<EmailTaskResponse> getTasks(Authentication authentication) {
    Long userId = getUserId(authentication);
    return taskService.getTasks(userId);
  }

  @PatchMapping("/{taskId}/complete")
  public EmailTaskResponse markCompleted(
      Authentication authentication,
      @PathVariable Long taskId,
      @RequestParam(defaultValue = "true") boolean completed
  ) {
    Long userId = getUserId(authentication);
    return taskService.markCompleted(userId, taskId, completed);
  }

  private Long getUserId(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authentication");
    }
    String email = authentication.getName();
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    return user.getId();
  }
}
