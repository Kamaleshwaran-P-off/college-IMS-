package com.smartcampus.platform.assignment.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.smartcampus.platform.assignment.dto.AssignmentRequest;
import com.smartcampus.platform.assignment.dto.AssignmentResponse;
import com.smartcampus.platform.assignment.service.AssignmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/assignments")
@Validated
public class AssignmentController {
  private final AssignmentService assignmentService;

  public AssignmentController(AssignmentService assignmentService) {
    this.assignmentService = assignmentService;
  }

  @PostMapping
  public ResponseEntity<AssignmentResponse> create(@Valid @RequestBody AssignmentRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(assignmentService.create(request));
  }

  @GetMapping
  public List<AssignmentResponse> getAll() {
    return assignmentService.findAll();
  }

  @GetMapping("/{id}")
  public AssignmentResponse getById(@PathVariable Long id) {
    return assignmentService.findById(id);
  }

  @PutMapping("/{id}")
  public AssignmentResponse update(@PathVariable Long id, @Valid @RequestBody AssignmentRequest request) {
    return assignmentService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    assignmentService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
