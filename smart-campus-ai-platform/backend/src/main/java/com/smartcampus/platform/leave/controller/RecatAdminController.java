package com.smartcampus.platform.leave.controller;

import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartcampus.platform.leave.dto.LeaveResponse;
import com.smartcampus.platform.leave.dto.RecatAdminReviewRequest;
import com.smartcampus.platform.leave.service.LeaveRequestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/recat/admin")
@Validated
public class RecatAdminController {
  private final LeaveRequestService leaveRequestService;

  public RecatAdminController(LeaveRequestService leaveRequestService) {
    this.leaveRequestService = leaveRequestService;
  }

  @PutMapping("/review")
  public LeaveResponse review(
      Authentication authentication,
      @Valid @RequestBody RecatAdminReviewRequest request
  ) {
    return leaveRequestService.adminReviewRecat(
        request.getRequestId(),
        request.getStatus(),
        request.getAdminRemarks(),
        authentication.getName()
    );
  }
}
