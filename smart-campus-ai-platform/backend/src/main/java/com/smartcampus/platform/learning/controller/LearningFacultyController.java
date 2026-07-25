package com.smartcampus.platform.learning.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.learning.dto.LearningCourseSummary;
import com.smartcampus.platform.learning.dto.LearningUploadResponse;
import com.smartcampus.platform.learning.service.LearningService;

@RestController
@RequestMapping("/api/faculty/learning")
public class LearningFacultyController {
  private final LearningService learningService;
  private final UserRepository userRepository;

  public LearningFacultyController(LearningService learningService, UserRepository userRepository) {
    this.learningService = learningService;
    this.userRepository = userRepository;
  }

  @PostMapping(value = "/courses", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<LearningUploadResponse> uploadCourse(
      Authentication authentication,
      @RequestParam("title") String title,
      @RequestParam(value = "description", required = false) String description,
      @RequestParam("file") MultipartFile file
  ) {
    Long facultyId = ensureFaculty(authentication);
    var course = learningService.createCourse(facultyId, title, description, file);
    int topicCount = course.getTopics().size();
    return ResponseEntity.ok(new LearningUploadResponse(course.getId(), topicCount));
  }

  @GetMapping("/courses")
  public List<LearningCourseSummary> listCourses(Authentication authentication) {
    Long facultyId = ensureFaculty(authentication);
    return learningService.listFacultyCourses(facultyId);
  }

  private Long ensureFaculty(Authentication authentication) {
    String email = authentication.getName();
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF && user.getRole() != Role.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }
    return user.getId();
  }
}
