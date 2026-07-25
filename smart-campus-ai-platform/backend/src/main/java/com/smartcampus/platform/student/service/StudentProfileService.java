package com.smartcampus.platform.student.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.common.exception.ResourceNotFoundException;
import com.smartcampus.platform.student.entity.Student;
import com.smartcampus.platform.student.repository.StudentRepository;

@Service
@Transactional
public class StudentProfileService {
  private final StudentRepository studentRepository;
  private final UserRepository userRepository;

  public StudentProfileService(StudentRepository studentRepository, UserRepository userRepository) {
    this.studentRepository = studentRepository;
    this.userRepository = userRepository;
  }

  public Student getOrCreateByUserId(Long userId) {
    return studentRepository.findByUserId(userId)
        .orElseGet(() -> createDefaultProfile(loadUser(userId)));
  }

  public Student ensureForUser(User user) {
    if (user == null) {
      throw new ResourceNotFoundException("User not found");
    }
    return studentRepository.findByUserId(user.getId())
        .orElseGet(() -> createDefaultProfile(user));
  }

  private User loadUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }

  private Student createDefaultProfile(User user) {
    if (user.getRole() != Role.STUDENT) {
      throw new IllegalArgumentException("Student access required");
    }
    String studentCode = "AUTO-" + user.getId();
    Student student = new Student(user, studentCode, null, null, null, null, null);
    return studentRepository.save(student);
  }
}
