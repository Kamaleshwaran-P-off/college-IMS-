package com.smartcampus.platform.inbox;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartcampus.platform.inbox.dto.InboxBulkUpdateRequest;
import com.smartcampus.platform.inbox.dto.InboxEmailResponse;
import com.smartcampus.platform.inbox.dto.InboxEmailUpdateRequest;

@RestController
@RequestMapping("/api/inbox")
public class InboxController {
  private final InboxService inboxService;

  public InboxController(InboxService inboxService) {
    this.inboxService = inboxService;
  }

  @GetMapping
  public ResponseEntity<List<InboxEmailResponse>> listEmails() {
    return ResponseEntity.ok(inboxService.listEmails());
  }

  @PatchMapping("/{id}")
  public ResponseEntity<InboxEmailResponse> updateEmail(
      @PathVariable Long id,
      @RequestBody InboxEmailUpdateRequest request
  ) {
    return ResponseEntity.ok(inboxService.updateEmail(id, request));
  }

  @PatchMapping("/bulk")
  public ResponseEntity<List<InboxEmailResponse>> bulkUpdate(@RequestBody InboxBulkUpdateRequest request) {
    return ResponseEntity.ok(inboxService.bulkUpdate(request));
  }
}
