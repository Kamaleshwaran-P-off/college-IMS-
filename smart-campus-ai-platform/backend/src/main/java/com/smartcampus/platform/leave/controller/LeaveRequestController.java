package com.smartcampus.platform.leave.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.smartcampus.platform.leave.dto.LeaveCreateRequest;
import com.smartcampus.platform.leave.dto.LeaveDecisionRequest;
import com.smartcampus.platform.leave.dto.LeaveResponse;
import com.smartcampus.platform.leave.entity.LeaveStatus;
import com.smartcampus.platform.leave.entity.LeaveType;
import com.smartcampus.platform.leave.service.LeaveRequestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/leaves")
@Validated
public class LeaveRequestController {
  private final LeaveRequestService leaveRequestService;

  public LeaveRequestController(LeaveRequestService leaveRequestService) {
    this.leaveRequestService = leaveRequestService;
  }

  @PostMapping
  public ResponseEntity<LeaveResponse> create(@Valid @RequestBody LeaveCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(leaveRequestService.create(request));
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<LeaveResponse> createWithFiles(
      @RequestParam Long userId,
      @RequestParam com.smartcampus.platform.leave.entity.LeaveType type,
      @RequestParam String startDate,
      @RequestParam(required = false) String endDate,
      @RequestParam(required = false) String reason,
      @RequestParam(required = false) MultipartFile proofFile,
      @RequestParam(required = false) MultipartFile applicationLetter
  ) throws java.io.IOException {
    LeaveCreateRequest request = new LeaveCreateRequest();
    request.setUserId(userId);
    request.setType(type);
    request.setStartDate(java.time.LocalDate.parse(startDate));
    request.setEndDate(endDate != null && !endDate.isBlank() ? java.time.LocalDate.parse(endDate) : null);
    request.setReason(reason);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(leaveRequestService.createWithFiles(request, proofFile, applicationLetter));
  }

  @GetMapping
  public List<LeaveResponse> list(
      @RequestParam(required = false) Long studentId,
      @RequestParam(required = false) Long mentorId,
      @RequestParam(required = false) LeaveStatus status,
      @RequestParam(required = false) LeaveType type
  ) {
    return leaveRequestService.findAll(studentId, mentorId, status, type);
  }

  @PatchMapping("/{id}/approve")
  public LeaveResponse approve(@PathVariable Long id, @Valid @RequestBody LeaveDecisionRequest request) {
    return leaveRequestService.approve(id, request);
  }

  @PatchMapping("/{id}/reject")
  public LeaveResponse reject(@PathVariable Long id, @Valid @RequestBody LeaveDecisionRequest request) {
    return leaveRequestService.reject(id, request);
  }

  @GetMapping("/{id}/proof")
  public ResponseEntity<byte[]> downloadProof(Authentication authentication, @PathVariable Long id) {
    var leave = leaveRequestService.getProof(id, authentication.getName());
    if (leave.getProofFileData() == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + leave.getProofFileName() + "\"")
        .contentType(MediaType.parseMediaType(leave.getProofContentType()))
        .body(leave.getProofFileData());
  }

  @GetMapping("/{id}/letter")
  public ResponseEntity<byte[]> downloadLetter(Authentication authentication, @PathVariable Long id) {
    var leave = leaveRequestService.getLetter(id, authentication.getName());
    if (leave.getLetterFileData() == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + leave.getLetterFileName() + "\"")
        .contentType(MediaType.parseMediaType(leave.getLetterContentType()))
        .body(leave.getLetterFileData());
  }
}
