package com.smartcampus.platform.reports.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartcampus.platform.reports.service.GradeExportService;

@RestController
@RequestMapping("/api")
public class GradeExportController {
  private final GradeExportService gradeExportService;

  public GradeExportController(GradeExportService gradeExportService) {
    this.gradeExportService = gradeExportService;
  }

  @GetMapping("/export-grades")
  public ResponseEntity<byte[]> exportGrades(
      Authentication authentication,
      @RequestParam String subject,
      @RequestParam String department,
      @RequestParam String section,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
  ) {
    byte[] data = gradeExportService.exportGrades(
        authentication.getName(),
        subject,
        department,
        section,
        startDate,
        endDate
    );

    String filename = String.format("grades-%s-%s-%s.csv", subject, department, section);

    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType("text/csv"))
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
        .body(data);
  }
}
