package com.smartcampus.platform.coursework.assignment.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartcampus.platform.coursework.assignment.dto.AssignmentGradeRequest;
import com.smartcampus.platform.coursework.assignment.dto.AssignmentSubmissionResponse;
import com.smartcampus.platform.coursework.assignment.dto.CourseAssignmentResponse;
import com.smartcampus.platform.coursework.assignment.entity.CourseAssignment;
import com.smartcampus.platform.coursework.assignment.service.CourseAssignmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/coursework/assignments")
@Validated
public class CourseAssignmentController {
  private static final Logger log = LoggerFactory.getLogger(CourseAssignmentController.class);
  private final CourseAssignmentService assignmentService;

  public CourseAssignmentController(CourseAssignmentService assignmentService) {
    this.assignmentService = assignmentService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public CourseAssignmentResponse createAssignment(
      Authentication authentication,
      @RequestParam("title") String title,
      @RequestParam(value = "description", required = false) String description,
      @RequestParam(value = "dueDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
      @RequestParam(value = "department", required = false) String department,
      @RequestParam("className") String className,
      @RequestParam(value = "file", required = false) MultipartFile file
  ) throws IOException {
    log.info("Assignment upload: title={}, class={}, fileName={}, size={}, type={}",
        title,
        className,
        file != null ? file.getOriginalFilename() : "none",
        file != null ? file.getSize() : 0,
        file != null ? file.getContentType() : "none");
    return assignmentService.createAssignment(
        title,
        description,
        dueDate,
        department,
        className,
        file,
        authentication.getName()
    );
  }

  @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public CourseAssignmentResponse updateAssignment(
      Authentication authentication,
      @PathVariable Long id,
      @RequestParam(value = "title", required = false) String title,
      @RequestParam(value = "description", required = false) String description,
      @RequestParam(value = "dueDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueDate,
      @RequestParam(value = "department", required = false) String department,
      @RequestParam(value = "className", required = false) String className,
      @RequestParam(value = "file", required = false) MultipartFile file
  ) throws IOException {
    return assignmentService.updateAssignment(
        id,
        title,
        description,
        dueDate,
        department,
        className,
        file,
        authentication.getName()
    );
  }

  @PatchMapping("/{id}/hide")
  public CourseAssignmentResponse updateVisibility(
      Authentication authentication,
      @PathVariable Long id,
      @RequestParam(value = "visible", required = false) Boolean visible
  ) {
    return assignmentService.updateVisibility(id, visible, authentication.getName());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteAssignment(
      Authentication authentication,
      @PathVariable Long id
  ) {
    assignmentService.deleteAssignment(id, authentication.getName());
    return ResponseEntity.noContent().build();
  }

  @GetMapping
  public List<CourseAssignmentResponse> getAssignments(Authentication authentication) {
    return assignmentService.getAssignments(authentication.getName());
  }

  @GetMapping("/{id}/file")
  public ResponseEntity<byte[]> downloadAssignmentFile(
      Authentication authentication,
      @PathVariable Long id
  ) {
    CourseAssignment assignment = assignmentService.getAssignmentForDownload(id, authentication.getName());
    if (assignment.getFileData() == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + assignment.getFileName() + "\"")
        .contentType(MediaType.parseMediaType(assignment.getContentType()))
        .body(assignment.getFileData());
  }

  @PostMapping(value = "/{id}/submissions", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public AssignmentSubmissionResponse submitAssignment(
      Authentication authentication,
      @PathVariable Long id,
      @RequestParam(value = "answerText", required = false) String answerText,
      @RequestParam(value = "file", required = false) MultipartFile file
  ) throws IOException {
    log.info("Assignment submission: assignmentId={}, fileName={}, size={}, type={}",
        id,
        file != null ? file.getOriginalFilename() : "none",
        file != null ? file.getSize() : 0,
        file != null ? file.getContentType() : "none");
    return assignmentService.submitAssignment(id, answerText, file, authentication.getName());
  }

  @GetMapping("/{id}/submissions")
  public List<AssignmentSubmissionResponse> getSubmissions(
      Authentication authentication,
      @PathVariable Long id
  ) {
    return assignmentService.getSubmissions(id, authentication.getName());
  }

  @GetMapping("/submissions/mine")
  public List<AssignmentSubmissionResponse> getMySubmissions(Authentication authentication) {
    return assignmentService.getStudentSubmissions(authentication.getName());
  }

  @PatchMapping("/submissions/{id}")
  public AssignmentSubmissionResponse gradeSubmission(
      Authentication authentication,
      @PathVariable Long id,
      @Valid @org.springframework.web.bind.annotation.RequestBody AssignmentGradeRequest request
  ) {
    return assignmentService.gradeSubmission(id, request, authentication.getName());
  }

  @GetMapping("/submissions/{id}/file")
  public ResponseEntity<byte[]> downloadSubmissionFile(
      Authentication authentication,
      @PathVariable Long id
  ) {
    var submission = assignmentService.getSubmissionForDownload(id, authentication.getName());
    if (submission.getFileData() == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + submission.getFileName() + "\"")
        .contentType(MediaType.parseMediaType(submission.getContentType()))
        .body(submission.getFileData());
  }
}
