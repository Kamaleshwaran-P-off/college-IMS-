package com.smartcampus.platform.aiquiz.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.smartcampus.platform.aiquiz.entity.AiQuizSubmission;
import com.smartcampus.platform.aiquiz.service.AiQuizService;

@RestController
@RequestMapping("/api/certificate")
public class CertificateController {
  private final AiQuizService quizService;

  public CertificateController(AiQuizService quizService) {
    this.quizService = quizService;
  }

  @GetMapping("/{quizId}")
  public ResponseEntity<byte[]> downloadCertificate(
      Authentication authentication,
      @PathVariable Long quizId
  ) {
    AiQuizSubmission submission = quizService.getCertificateForQuiz(quizId, authentication.getName());
    if (submission.getCertificateData() == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + submission.getCertificateFileName() + "\"")
        .contentType(MediaType.parseMediaType(submission.getCertificateContentType()))
        .body(submission.getCertificateData());
  }
}
