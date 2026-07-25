package com.smartcampus.platform.learning.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.learning.dto.LearningChatRequest;
import com.smartcampus.platform.learning.dto.LearningChatResponse;
import com.smartcampus.platform.learning.dto.LearningCourseDetailResponse;
import com.smartcampus.platform.learning.dto.LearningCourseSummary;
import com.smartcampus.platform.learning.dto.LearningProgressResponse;
import com.smartcampus.platform.learning.dto.QuizResponse;
import com.smartcampus.platform.learning.dto.QuizSubmitRequest;
import com.smartcampus.platform.learning.dto.QuizSubmitResponse;
import com.smartcampus.platform.learning.service.LearningService;

@RestController
@RequestMapping("/api/student/learning")
public class LearningStudentController {
  private static final Logger log = LoggerFactory.getLogger(LearningStudentController.class);
  private final LearningService learningService;
  private final UserRepository userRepository;

  public LearningStudentController(LearningService learningService, UserRepository userRepository) {
    this.learningService = learningService;
    this.userRepository = userRepository;
  }

  @GetMapping("/courses")
  public List<LearningCourseSummary> listCourses(Authentication authentication) {
    ensureStudent(authentication);
    return learningService.listStudentCourses();
  }

  @GetMapping("/courses/{courseId}/topics")
  public LearningCourseDetailResponse getTopics(
      Authentication authentication,
      @PathVariable Long courseId
  ) {
    Long studentId = ensureStudent(authentication);
    return learningService.getCourseTopics(studentId, courseId);
  }

  @PostMapping("/topics/{topicId}/quiz")
  public QuizResponse startQuiz(Authentication authentication, @PathVariable Long topicId) {
    Long studentId = ensureStudent(authentication);
    return learningService.getQuizForTopic(studentId, topicId);
  }

  @PostMapping("/topics/{topicId}/quiz/submit")
  public QuizSubmitResponse submitQuiz(
      Authentication authentication,
      @PathVariable Long topicId,
      @RequestBody QuizSubmitRequest request
  ) {
    Long studentId = ensureStudent(authentication);
    return learningService.submitQuiz(studentId, topicId, request);
  }

  @GetMapping("/progress")
  public LearningProgressResponse getProgress(Authentication authentication) {
    Long studentId = ensureStudent(authentication);
    return learningService.getProgress(studentId);
  }

  @PostMapping("/topics/{topicId}/chat")
  public ResponseEntity<LearningChatResponse> chat(
      Authentication authentication,
      @PathVariable Long topicId,
      @RequestBody LearningChatRequest request
  ) {
    ensureStudent(authentication);
    String reply = learningService.chatOnTopic(topicId, request.getMessage());
    return ResponseEntity.ok(new LearningChatResponse(reply));
  }

  private Long ensureStudent(Authentication authentication) {
    String email = authentication.getName();
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    log.debug("Student API access: email={}, role={}", user.getEmail(), user.getRole());
    if (user.getRole() != Role.STUDENT) {
      log.warn("Student API denied: email={}, role={}", user.getEmail(), user.getRole());
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
    }
    return user.getId();
  }
}
