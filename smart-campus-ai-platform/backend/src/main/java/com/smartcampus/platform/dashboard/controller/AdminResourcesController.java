package com.smartcampus.platform.dashboard.controller;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.calendar.entity.AcademicCalendar;
import com.smartcampus.platform.calendar.repository.AcademicCalendarRepository;
import com.smartcampus.platform.timetable.entity.Timetable;
import com.smartcampus.platform.timetable.repository.TimetableRepository;

@RestController
@RequestMapping("/api/admin")
public class AdminResourcesController {
  private static final Logger log = LoggerFactory.getLogger(AdminResourcesController.class);
  private final UserRepository userRepository;
  private final AcademicCalendarRepository academicCalendarRepository;
  private final TimetableRepository timetableRepository;

  public AdminResourcesController(
      UserRepository userRepository,
      AcademicCalendarRepository academicCalendarRepository,
      TimetableRepository timetableRepository
  ) {
    this.userRepository = userRepository;
    this.academicCalendarRepository = academicCalendarRepository;
    this.timetableRepository = timetableRepository;
  }

  @PostMapping(value = "/upload-calendar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Map<String, Object>> uploadCalendar(
      Authentication authentication,
      @RequestParam("file") MultipartFile file
  ) throws IOException {
    ensureAdmin(authentication);
    log.info("Calendar upload received: name={}, size={}, type={}",
        file != null ? file.getOriginalFilename() : "null",
        file != null ? file.getSize() : 0,
        file != null ? file.getContentType() : "null");
    if (file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Calendar file is required.");
    }

    AcademicCalendar calendar = new AcademicCalendar(
        file.getOriginalFilename(),
        file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType(),
        file.getBytes(),
        LocalDateTime.now()
    );

    AcademicCalendar saved = academicCalendarRepository.save(calendar);
    return ResponseEntity.ok(Map.of("id", saved.getId(), "fileName", saved.getFileName()));
  }

  @PostMapping(value = "/upload-timetable", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<Map<String, Object>> uploadTimetable(
      Authentication authentication,
      @RequestParam("department") String department,
      @RequestParam("section") String section,
      @RequestParam("file") MultipartFile file
  ) throws IOException {
    ensureAdmin(authentication);
    log.info("Timetable upload received: dept={}, section={}, name={}, size={}, type={}",
        department,
        section,
        file != null ? file.getOriginalFilename() : "null",
        file != null ? file.getSize() : 0,
        file != null ? file.getContentType() : "null");
    if (file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Timetable file is required.");
    }

    Timetable timetable = new Timetable(
        department,
        section,
        file.getOriginalFilename(),
        file.getContentType() == null ? MediaType.APPLICATION_OCTET_STREAM_VALUE : file.getContentType(),
        file.getBytes(),
        LocalDateTime.now()
    );

    Timetable saved = timetableRepository.save(timetable);
    return ResponseEntity.ok(Map.of("id", saved.getId(), "fileName", saved.getFileName()));
  }

  @GetMapping("/academic-calendar/latest")
  public ResponseEntity<byte[]> getLatestCalendar() {
    AcademicCalendar calendar = academicCalendarRepository.findTopByOrderByUploadedAtDesc()
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No calendar uploaded"));

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + calendar.getFileName() + "\"")
        .contentType(MediaType.parseMediaType(calendar.getContentType()))
        .body(calendar.getFileData());
  }

  @GetMapping("/timetable/latest")
  public ResponseEntity<byte[]> getLatestTimetable(
      @RequestParam("department") String department,
      @RequestParam("section") String section
  ) {
    Timetable timetable = timetableRepository.findTopByDepartmentAndSectionOrderByUploadedAtDesc(department, section)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No timetable uploaded"));

    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + timetable.getFileName() + "\"")
        .contentType(MediaType.parseMediaType(timetable.getContentType()))
        .body(timetable.getFileData());
  }

  private void ensureAdmin(Authentication authentication) {
    String email = authentication.getName();
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    if (user.getRole() != Role.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
    }
  }
}
