package com.smartcampus.platform.user.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.smartcampus.platform.user.dto.UserCreateRequest;
import com.smartcampus.platform.user.dto.UserResponse;
import com.smartcampus.platform.user.dto.UserUpdateRequest;
import com.smartcampus.platform.user.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping
  public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(request));
  }

  @GetMapping
  public List<UserResponse> getAll() {
    return userService.findAll();
  }

  @GetMapping("/{id}")
  public UserResponse getById(@PathVariable Long id) {
    return userService.findById(id);
  }

  @PutMapping("/{id}")
  public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request) {
    return userService.update(id, request);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable Long id) {
    userService.delete(id);
    return ResponseEntity.noContent().build();
  }
}
