package com.smartcampus.platform.student.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.common.exception.ResourceNotFoundException;
import com.smartcampus.platform.student.dto.StudentRequest;
import com.smartcampus.platform.student.dto.StudentResponse;
import com.smartcampus.platform.student.entity.Student;
import com.smartcampus.platform.student.repository.StudentRepository;
import com.smartcampus.platform.student.service.StudentProfileService;

@Service
@Transactional
public class StudentService {
  private final StudentRepository studentRepository;
  private final UserRepository userRepository;
  private final StudentProfileService studentProfileService;

  public StudentService(StudentRepository studentRepository, UserRepository userRepository, StudentProfileService studentProfileService) {
    this.studentRepository = studentRepository;
    this.userRepository = userRepository;
    this.studentProfileService = studentProfileService;
  }

  public StudentResponse create(StudentRequest request) {
    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (user.getRole() != Role.STUDENT) {
      throw new IllegalArgumentException("User role must be STUDENT to create a student profile");
    }

    if (studentRepository.existsByUserId(user.getId())) {
      throw new IllegalArgumentException("Student profile already exists for this user");
    }

    Student student = new Student(
        user,
        request.getStudentCode(),
        request.getDepartment(),
        request.getYearOfStudy(),
        request.getSection(),
        request.getPhone(),
        request.getParentPhone()
    );

    return toResponse(studentRepository.save(student));
  }

  public List<StudentResponse> findAll() {
    return studentRepository.findAll().stream().map(this::toResponse).toList();
  }

  public StudentResponse findById(Long id) {
    return toResponse(getStudent(id));
  }

  public StudentResponse findByUserId(Long userId) {
    Student student = studentProfileService.getOrCreateByUserId(userId);
    return toResponse(student);
  }

  public StudentResponse update(Long id, StudentRequest request) {
    Student student = getStudent(id);
    if (!student.getUser().getId().equals(request.getUserId())) {
      User user = userRepository.findById(request.getUserId())
          .orElseThrow(() -> new ResourceNotFoundException("User not found"));
      if (user.getRole() != Role.STUDENT) {
        throw new IllegalArgumentException("User role must be STUDENT to create a student profile");
      }
      if (studentRepository.existsByUserId(user.getId())) {
        throw new IllegalArgumentException("Student profile already exists for this user");
      }
      student.setUser(user);
    }

    student.setStudentCode(request.getStudentCode());
    student.setDepartment(request.getDepartment());
    student.setYearOfStudy(request.getYearOfStudy());
    student.setSection(request.getSection());
    student.setPhone(request.getPhone());
    student.setParentPhone(request.getParentPhone());

    return toResponse(studentRepository.save(student));
  }

  public void delete(Long id) {
    Student student = getStudent(id);
    studentRepository.delete(student);
  }

  private Student getStudent(Long id) {
    return studentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
  }

  private StudentResponse toResponse(Student student) {
    return new StudentResponse(
        student.getId(),
        student.getUser().getId(),
        student.getUser().getFullName(),
        student.getUser().getEmail(),
        student.getStudentCode(),
        student.getDepartment(),
        student.getYearOfStudy(),
        student.getSection(),
        student.getPhone(),
        student.getParentPhone()
    );
  }
}
