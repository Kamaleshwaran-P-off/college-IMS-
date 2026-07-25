package com.smartcampus.platform.answer.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.smartcampus.platform.answer.dto.AnswerRequest;
import com.smartcampus.platform.answer.dto.AnswerResponse;
import com.smartcampus.platform.answer.service.AnswerService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/answers")
@Validated
public class AnswerController {
  private final AnswerService answerService;

  public AnswerController(AnswerService answerService) {
    this.answerService = answerService;
  }

  @PostMapping
  public ResponseEntity<AnswerResponse> create(@Valid @RequestBody AnswerRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(answerService.create(request));
  }

  @GetMapping
  public List<AnswerResponse> getAll() {
    return answerService.findAll();
  }

  @GetMapping("/{id}")
  public AnswerResponse getById(@PathVariable Long id) {
    return answerService.findById(id);
  }

  @PutMapping("/{id}")
  public AnswerResponse update(@PathVariable Long id, @Valid @RequestBody AnswerRequest request) {
    return answerService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    answerService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
