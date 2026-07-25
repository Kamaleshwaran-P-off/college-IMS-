package com.smartcampus.platform.queryusage.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.smartcampus.platform.queryusage.dto.QueryUsageRequest;
import com.smartcampus.platform.queryusage.dto.QueryUsageResponse;
import com.smartcampus.platform.queryusage.service.QueryUsageService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/query-usage")
@Validated
public class QueryUsageController {
  private final QueryUsageService queryUsageService;

  public QueryUsageController(QueryUsageService queryUsageService) {
    this.queryUsageService = queryUsageService;
  }

  @PostMapping
  public ResponseEntity<QueryUsageResponse> create(@Valid @RequestBody QueryUsageRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(queryUsageService.create(request));
  }

  @GetMapping
  public List<QueryUsageResponse> getAll() {
    return queryUsageService.findAll();
  }

  @GetMapping("/{id}")
  public QueryUsageResponse getById(@PathVariable Long id) {
    return queryUsageService.findById(id);
  }

  @PutMapping("/{id}")
  public QueryUsageResponse update(@PathVariable Long id, @Valid @RequestBody QueryUsageRequest request) {
    return queryUsageService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    queryUsageService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
