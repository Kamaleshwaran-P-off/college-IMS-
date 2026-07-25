package com.smartcampus.platform.doubt.controller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.smartcampus.platform.common.dto.PagedResponse;
import com.smartcampus.platform.doubt.dto.DoubtDetailResponse;
import com.smartcampus.platform.doubt.dto.DoubtRequest;
import com.smartcampus.platform.doubt.dto.DoubtResponse;
import com.smartcampus.platform.doubt.dto.LeaderboardEntry;
import com.smartcampus.platform.doubt.entity.DoubtStatus;
import com.smartcampus.platform.doubt.service.DoubtService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/doubts")
@Validated
public class DoubtController {
  private final DoubtService doubtService;

  public DoubtController(DoubtService doubtService) {
    this.doubtService = doubtService;
  }

  @PostMapping
  public ResponseEntity<DoubtResponse> create(@Valid @RequestBody DoubtRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(doubtService.create(request));
  }

  @GetMapping
  public PagedResponse<DoubtResponse> getAll(
      @RequestParam(required = false) DoubtStatus status,
      @RequestParam(required = false) Long assignmentId,
      @RequestParam(required = false) Long studentUserId,
      @RequestParam(required = false) Boolean accepted,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size
  ) {
    String normalizedSearch = (search != null && !search.isBlank()) ? search : null;
    PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    return doubtService.findAll(status, assignmentId, studentUserId, accepted, normalizedSearch, pageable);
  }

  @GetMapping("/{id}")
  public DoubtDetailResponse getById(@PathVariable Long id) {
    return doubtService.findById(id);
  }

  @PostMapping("/{id}/accept/{answerId}")
  public DoubtDetailResponse acceptAnswer(@PathVariable Long id, @PathVariable Long answerId) {
    return doubtService.acceptBestAnswer(id, answerId);
  }

  @GetMapping("/leaderboard")
  public List<LeaderboardEntry> leaderboard(@RequestParam(defaultValue = "5") int limit) {
    return doubtService.getLeaderboard(limit);
  }

  @PutMapping("/{id}")
  public DoubtResponse update(@PathVariable Long id, @Valid @RequestBody DoubtRequest request) {
    return doubtService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    doubtService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
