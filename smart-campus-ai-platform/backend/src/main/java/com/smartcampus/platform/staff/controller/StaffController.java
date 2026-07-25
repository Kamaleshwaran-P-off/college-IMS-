package com.smartcampus.platform.staff.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.smartcampus.platform.staff.dto.StaffRequest;
import com.smartcampus.platform.staff.dto.StaffResponse;
import com.smartcampus.platform.staff.service.StaffService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/staff")
@Validated
public class StaffController {
  private final StaffService staffService;

  public StaffController(StaffService staffService) {
    this.staffService = staffService;
  }

  @PostMapping
  public ResponseEntity<StaffResponse> create(@Valid @RequestBody StaffRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(staffService.create(request));
  }

  @GetMapping
  public List<StaffResponse> getAll() {
    return staffService.findAll();
  }

  @GetMapping("/{id}")
  public StaffResponse getById(@PathVariable Long id) {
    return staffService.findById(id);
  }

  @GetMapping("/by-user")
  public StaffResponse getByUser(@RequestParam Long userId) {
    return staffService.findByUserId(userId);
  }

  @PutMapping("/{id}")
  public StaffResponse update(@PathVariable Long id, @Valid @RequestBody StaffRequest request) {
    return staffService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    staffService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
