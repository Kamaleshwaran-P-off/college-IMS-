package com.smartcampus.platform.assignmentplanner.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.assignmentplanner.dto.ScheduleGenerateRequest;
import com.smartcampus.platform.assignmentplanner.dto.ScheduledTaskResponse;
import com.smartcampus.platform.assignmentplanner.service.SmartSchedulerService;
import com.smartcampus.platform.auth.repository.UserRepository;

@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {
  private final SmartSchedulerService schedulerService;
  private final UserRepository userRepository;

  public ScheduleController(SmartSchedulerService schedulerService, UserRepository userRepository) {
    this.schedulerService = schedulerService;
    this.userRepository = userRepository;
  }

  @PostMapping("/generate")
  public List<ScheduledTaskResponse> generate(
      Authentication authentication,
      @RequestBody(required = false) ScheduleGenerateRequest request
  ) {
    var user = userRepository.findByEmail(authentication.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    Integer maxHours = request != null ? request.getMaxHoursPerDay() : null;
    return schedulerService.generateSchedule(user, maxHours);
  }

  @GetMapping("/{studentId}")
  public List<ScheduledTaskResponse> getSchedule(
      Authentication authentication,
      @PathVariable Long studentId
  ) {
    var user = userRepository.findByEmail(authentication.getName())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    return schedulerService.getScheduleForStudent(user, studentId);
  }
}
