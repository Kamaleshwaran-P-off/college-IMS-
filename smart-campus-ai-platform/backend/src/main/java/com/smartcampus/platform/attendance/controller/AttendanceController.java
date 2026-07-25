package com.smartcampus.platform.attendance.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;

import com.smartcampus.platform.attendance.dto.AttendanceRequest;
import com.smartcampus.platform.attendance.dto.AttendanceResponse;
import com.smartcampus.platform.attendance.service.AttendanceService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/attendance")
@Validated
public class AttendanceController {
  private final AttendanceService attendanceService;

  public AttendanceController(AttendanceService attendanceService) {
    this.attendanceService = attendanceService;
  }

  @PostMapping
  public ResponseEntity<AttendanceResponse> create(
      Authentication authentication,
      @Valid @RequestBody AttendanceRequest request
  ) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(attendanceService.create(request, authentication.getName()));
  }

  @GetMapping
  public List<AttendanceResponse> getClassAttendance(
      Authentication authentication,
      @RequestParam String subject,
      @RequestParam String department,
      @RequestParam String section,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) java.time.LocalDate endDate
  ) {
    return attendanceService.getClassAttendance(
        authentication.getName(),
        subject,
        department,
        section,
        startDate,
        endDate
    );
  }

  @GetMapping("/mine")
  public List<AttendanceResponse> getStudentAttendance(
      Authentication authentication,
      @RequestParam(required = false) String subject
  ) {
    return attendanceService.getStudentAttendance(authentication.getName(), subject);
  }

  @PutMapping("/{id}")
  public AttendanceResponse update(
      Authentication authentication,
      @PathVariable Long id,
      @Valid @RequestBody AttendanceRequest request
  ) {
    return attendanceService.update(id, request, authentication.getName());
  }
}
