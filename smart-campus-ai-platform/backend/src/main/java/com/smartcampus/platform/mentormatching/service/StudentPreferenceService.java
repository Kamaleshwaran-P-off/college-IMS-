package com.smartcampus.platform.mentormatching.service;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.mentormatching.dto.StudentPreferenceRequest;
import com.smartcampus.platform.mentormatching.dto.StudentPreferenceResponse;
import com.smartcampus.platform.mentormatching.entity.StudentPreference;
import com.smartcampus.platform.mentormatching.repository.StudentPreferenceRepository;
import com.smartcampus.platform.student.entity.Student;
import com.smartcampus.platform.student.service.StudentProfileService;

@Service
@Transactional
public class StudentPreferenceService {
  private final StudentPreferenceRepository preferenceRepository;
  private final UserRepository userRepository;
  private final StudentProfileService studentProfileService;

  public StudentPreferenceService(
      StudentPreferenceRepository preferenceRepository,
      UserRepository userRepository,
      StudentProfileService studentProfileService
  ) {
    this.preferenceRepository = preferenceRepository;
    this.userRepository = userRepository;
    this.studentProfileService = studentProfileService;
  }

  public StudentPreferenceResponse upsert(String studentEmail, StudentPreferenceRequest request) {
    var user = userRepository.findByEmail(studentEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STUDENT) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
    }

    Student student = studentProfileService.ensureForUser(user);

    StudentPreference preference = preferenceRepository.findByStudentId(student.getId())
        .orElseGet(() -> new StudentPreference(student, "", "", "", "", LocalDateTime.now()));

    preference.setRequiredSkills(request.getRequiredSkills());
    preference.setLearningGoals(request.getLearningGoals());
    preference.setMentorType(request.getMentorType());
    preference.setAvailability(request.getAvailability());
    preference.setUpdatedAt(LocalDateTime.now());

    StudentPreference saved = preferenceRepository.save(preference);
    return toResponse(saved);
  }

  public StudentPreferenceResponse getPreferences(String studentEmail) {
    var user = userRepository.findByEmail(studentEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STUDENT) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
    }
    Student student = studentProfileService.ensureForUser(user);

    StudentPreference preference = preferenceRepository.findByStudentId(student.getId())
        .orElseGet(() -> new StudentPreference(student, "", "", "", "", LocalDateTime.now()));
    return toResponse(preference);
  }

  private StudentPreferenceResponse toResponse(StudentPreference preference) {
    return new StudentPreferenceResponse(
        preference.getId(),
        preference.getStudent().getId(),
        preference.getStudent().getUser().getFullName(),
        preference.getRequiredSkills(),
        preference.getLearningGoals(),
        preference.getMentorType(),
        preference.getAvailability()
    );
  }
}
