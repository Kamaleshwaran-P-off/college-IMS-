package com.smartcampus.platform.aiquiz.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.smartcampus.platform.aiquiz.dto.AiQuizGenerateRequest;
import com.smartcampus.platform.aiquiz.dto.AiQuizResponse;
import com.smartcampus.platform.aiquiz.dto.AiQuizSubmissionRequest;
import com.smartcampus.platform.aiquiz.dto.AiQuizSubmissionResponse;
import com.smartcampus.platform.aiquiz.dto.AiQuizUpdateRequest;
import com.smartcampus.platform.aiquiz.entity.AiQuizSubmission;
import com.smartcampus.platform.aiquiz.service.AiQuizService;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/ai-quiz")
@Validated
public class AiQuizController {
  private static final Logger log = LoggerFactory.getLogger(AiQuizController.class);
  private final AiQuizService quizService;

  public AiQuizController(AiQuizService quizService) {
    this.quizService = quizService;
  }

  @PostMapping("/generate")
  public AiQuizResponse generateQuiz(
      Authentication authentication,
      @Valid @RequestBody AiQuizGenerateRequest request
  ) {
    return quizService.generateQuiz(request, authentication.getName());
  }

  @PostMapping(value = "/generate-pdf", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public AiQuizResponse generateQuizFromPdf(
      Authentication authentication,
      @RequestParam("file") MultipartFile file,
      @RequestParam("questionCount") Integer questionCount,
      @RequestParam("questionTypes") String questionTypes,
      @RequestParam(value = "durationMinutes", required = false) Integer durationMinutes,
      @RequestParam(value = "title", required = false) String title,
      @RequestParam(value = "className", required = false) String className,
      @RequestParam(value = "department", required = false) String department
  ) throws java.io.IOException {
    java.util.List<String> types = java.util.Arrays.stream(questionTypes.split(","))
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .toList();
    return quizService.generateQuizFromPdf(
        file,
        questionCount,
        types,
        durationMinutes,
        title,
        className,
        department,
        authentication.getName()
    );
  }

  @PatchMapping("/{id}/hide")
  public AiQuizResponse updateVisibility(
      Authentication authentication,
      @PathVariable Long id,
      @RequestParam(value = "visible", required = false) Boolean visible
  ) {
    return quizService.updateVisibility(id, visible, authentication.getName());
  }

  @PutMapping("/{id}")
  public AiQuizResponse updateQuiz(
      Authentication authentication,
      @PathVariable Long id,
      @RequestBody AiQuizUpdateRequest request
  ) {
    return quizService.updateQuiz(
        id,
        authentication.getName(),
        request.getTitle(),
        request.getSyllabus(),
        request.getDepartment(),
        request.getClassName(),
        request.getDurationMinutes()
    );
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteQuiz(
      Authentication authentication,
      @PathVariable Long id
  ) {
    quizService.deleteQuiz(id, authentication.getName());
    return ResponseEntity.noContent().build();
  }

  @GetMapping("/latest")
  public AiQuizResponse getLatest(Authentication authentication) {
    String email = authentication.getName();
    String role = authentication.getAuthorities().stream()
        .map(authority -> authority.getAuthority())
        .findFirst()
        .orElse("UNKNOWN");

    log.info("AI quiz latest requested by {} with role {}", email, role);

    if ("ROLE_STAFF".equals(role)) {
      return quizService.getLatestForFaculty(email);
    }
    if ("ROLE_STUDENT".equals(role)) {
      return quizService.getLatestForStudent(email);
    }
    log.warn("AI quiz access denied for {} with role {}", email, role);
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
  }

  @GetMapping("/student")
  public AiQuizResponse getLatestForStudent(Authentication authentication) {
    return quizService.getLatestForStudent(authentication.getName());
  }

  @GetMapping("/faculty")
  public AiQuizResponse getLatestForFaculty(Authentication authentication) {
    return quizService.getLatestForFaculty(authentication.getName());
  }

  @PostMapping("/submit")
  public AiQuizSubmissionResponse submitQuiz(
      Authentication authentication,
      @Valid @RequestBody AiQuizSubmissionRequest request
  ) {
    return quizService.submitQuiz(request, authentication.getName());
  }

  @GetMapping("/submissions/{id}/certificate")
  public ResponseEntity<byte[]> downloadCertificate(
      Authentication authentication,
      @PathVariable Long id
  ) {
    AiQuizSubmission submission = quizService.getCertificate(id, authentication.getName());
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + submission.getCertificateFileName() + "\"")
        .contentType(MediaType.parseMediaType(submission.getCertificateContentType()))
        .body(submission.getCertificateData());
  }
}
