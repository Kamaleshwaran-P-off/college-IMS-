package com.smartcampus.platform.planner.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.smartcampus.platform.planner.dto.StudyPlanHistoryResponse;
import com.smartcampus.platform.planner.dto.StudyPlanMultiRequest;
import com.smartcampus.platform.planner.dto.StudyPlanRequest;
import com.smartcampus.platform.planner.dto.StudyPlanResponse;
import com.smartcampus.platform.planner.dto.StudyStreakResponse;
import com.smartcampus.platform.planner.dto.StudyTaskResponse;
import com.smartcampus.platform.planner.service.StudyPlannerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/study-plans")
@Validated
public class StudyPlannerController {
  private final StudyPlannerService studyPlannerService;

  public StudyPlannerController(StudyPlannerService studyPlannerService) {
    this.studyPlannerService = studyPlannerService;
  }

  @PostMapping
  public ResponseEntity<StudyPlanResponse> create(@Valid @RequestBody StudyPlanRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(studyPlannerService.createPlan(request));
  }

  @PostMapping("/multi")
  public ResponseEntity<java.util.List<StudyPlanResponse>> createMulti(@Valid @RequestBody StudyPlanMultiRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(studyPlannerService.createMultiPlan(request));
  }

  @GetMapping("/latest")
  public ResponseEntity<StudyPlanResponse> latest(@RequestParam Long userId) {
    StudyPlanResponse response = studyPlannerService.getLatest(userId);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public java.util.List<StudyPlanHistoryResponse> history(@RequestParam Long userId) {
    return studyPlannerService.getHistory(userId);
  }

  @GetMapping("/streak")
  public StudyStreakResponse streak(@RequestParam Long userId) {
    return studyPlannerService.getStreak(userId);
  }

  @GetMapping("/{id}")
  public StudyPlanResponse getById(@PathVariable Long id) {
    return studyPlannerService.getById(id);
  }

  @GetMapping("/{id}/export/ics")
  public ResponseEntity<String> exportIcs(@PathVariable Long id) {
    String ics = studyPlannerService.exportCalendar(id);
    return ResponseEntity.ok()
        .header("Content-Type", "text/calendar")
        .header("Content-Disposition", "attachment; filename=study-plan-" + id + ".ics")
        .body(ics);
  }

  @PatchMapping("/tasks/{taskId}/complete")
  public StudyTaskResponse completeTask(@PathVariable Long taskId, @RequestParam boolean completed) {
    return studyPlannerService.updateTaskCompletion(taskId, completed);
  }

  @PatchMapping("/tasks/{taskId}/reminder")
  public StudyTaskResponse setReminder(@PathVariable Long taskId, @RequestParam(required = false) String reminderAt) {
    java.time.LocalDateTime reminder = reminderAt != null && !reminderAt.isBlank()
        ? java.time.LocalDateTime.parse(reminderAt)
        : null;
    return studyPlannerService.updateTaskReminder(taskId, reminder);
  }
}
