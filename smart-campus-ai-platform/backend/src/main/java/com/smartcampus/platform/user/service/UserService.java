package com.smartcampus.platform.user.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.common.exception.ResourceNotFoundException;
import com.smartcampus.platform.user.dto.UserCreateRequest;
import com.smartcampus.platform.user.dto.UserResponse;
import com.smartcampus.platform.user.dto.UserUpdateRequest;

@Service
@Transactional
public class UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public UserResponse create(UserCreateRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new IllegalArgumentException("Email already registered");
    }

    User user = new User(
        request.getFullName(),
        request.getEmail(),
        passwordEncoder.encode(request.getPassword()),
        request.getRole()
    );

    return toResponse(userRepository.save(user));
  }

  public List<UserResponse> findAll() {
    return userRepository.findAll().stream().map(this::toResponse).toList();
  }

  public UserResponse findById(Long id) {
    return toResponse(getUser(id));
  }

  public UserResponse update(Long id, UserUpdateRequest request) {
    User user = getUser(id);
    user.setFullName(request.getFullName());
    user.setRole(request.getRole());
    return toResponse(userRepository.save(user));
  }

  public void delete(Long id) {
    User user = getUser(id);
    userRepository.delete(user);
  }

  private User getUser(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }

  private UserResponse toResponse(User user) {
    return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), user.getRole().name());
  }
}
