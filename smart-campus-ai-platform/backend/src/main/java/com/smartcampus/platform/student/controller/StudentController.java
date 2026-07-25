package com.smartcampus.platform.student.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.smartcampus.platform.student.dto.StudentRequest;
import com.smartcampus.platform.student.dto.StudentResponse;
import com.smartcampus.platform.student.service.StudentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/students")
@Validated
public class StudentController {
  private final StudentService studentService;

  public StudentController(StudentService studentService) {
    this.studentService = studentService;
  }

  @PostMapping
  public ResponseEntity<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(studentService.create(request));
  }

  @GetMapping
  public List<StudentResponse> getAll() {
    return studentService.findAll();
  }

  @GetMapping("/{id}")
  public StudentResponse getById(@PathVariable Long id) {
    return studentService.findById(id);
  }

  @GetMapping("/by-user")
  public StudentResponse getByUser(@RequestParam Long userId) {
    return studentService.findByUserId(userId);
  }

  @PutMapping("/{id}")
  public StudentResponse update(@PathVariable Long id, @Valid @RequestBody StudentRequest request) {
    return studentService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    studentService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
