package com.smartcampus.platform.quiz.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.smartcampus.platform.quiz.dto.QuizQuestionsResponse;
import com.smartcampus.platform.quiz.dto.QuizSubmissionRequest;
import com.smartcampus.platform.quiz.dto.QuizSubmissionResponse;
import com.smartcampus.platform.quiz.service.QuizService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/quiz")
@Validated
public class QuizController {
  private final QuizService quizService;

  public QuizController(QuizService quizService) {
    this.quizService = quizService;
  }

  @GetMapping("/questions")
  public QuizQuestionsResponse getQuestions() {
    return quizService.getQuestions();
  }

  @PostMapping("/submit")
  public ResponseEntity<QuizSubmissionResponse> submit(@Valid @RequestBody QuizSubmissionRequest request) {
    return ResponseEntity.status(HttpStatus.OK).body(quizService.submit(request));
  }
}
