package com.smartcampus.platform.calendar.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.calendar.entity.AcademicCalendar;
import com.smartcampus.platform.calendar.repository.AcademicCalendarRepository;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {
  private final AcademicCalendarRepository academicCalendarRepository;

  public CalendarController(AcademicCalendarRepository academicCalendarRepository) {
    this.academicCalendarRepository = academicCalendarRepository;
  }

  @GetMapping("/latest")
  public ResponseEntity<byte[]> getLatestCalendar() {
    AcademicCalendar calendar = academicCalendarRepository.findTopByOrderByUploadedAtDesc()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No calendar uploaded"));

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + calendar.getFileName() + "\"")
        .contentType(MediaType.parseMediaType(calendar.getContentType()))
        .body(calendar.getFileData());
  }
}
