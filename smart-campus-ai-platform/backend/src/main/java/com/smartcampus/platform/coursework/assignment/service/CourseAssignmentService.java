package com.smartcampus.platform.coursework.assignment.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.coursework.assignment.dto.AssignmentGradeRequest;
import com.smartcampus.platform.coursework.assignment.dto.AssignmentSubmissionResponse;
import com.smartcampus.platform.coursework.assignment.dto.CourseAssignmentResponse;
import com.smartcampus.platform.coursework.assignment.entity.AssignmentSubmission;
import com.smartcampus.platform.coursework.assignment.entity.CourseAssignment;
import com.smartcampus.platform.coursework.assignment.repository.AssignmentSubmissionRepository;
import com.smartcampus.platform.coursework.assignment.repository.CourseAssignmentRepository;
import com.smartcampus.platform.defaultdata.RealisticDataGenerator;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.staff.repository.FacultyClassMappingRepository;
import com.smartcampus.platform.staff.service.StaffProfileService;
import com.smartcampus.platform.student.entity.Student;
import com.smartcampus.platform.student.service.StudentProfileService;

@Service
@Transactional
public class CourseAssignmentService {
  private final CourseAssignmentRepository assignmentRepository;
  private final AssignmentSubmissionRepository submissionRepository;
  private final UserRepository userRepository;
  private final StaffProfileService staffProfileService;
  private final StudentProfileService studentProfileService;
  private final FacultyClassMappingRepository mappingRepository;
  private final RealisticDataGenerator dataGenerator;

  public CourseAssignmentService(
      CourseAssignmentRepository assignmentRepository,
      AssignmentSubmissionRepository submissionRepository,
      UserRepository userRepository,
      StaffProfileService staffProfileService,
      StudentProfileService studentProfileService,
      FacultyClassMappingRepository mappingRepository,
      RealisticDataGenerator dataGenerator
  ) {
    this.assignmentRepository = assignmentRepository;
    this.submissionRepository = submissionRepository;
    this.userRepository = userRepository;
    this.staffProfileService = staffProfileService;
    this.studentProfileService = studentProfileService;
    this.mappingRepository = mappingRepository;
    this.dataGenerator = dataGenerator;
  }

  public CourseAssignmentResponse createAssignment(
      String title,
      String description,
      LocalDate dueDate,
      String department,
      String className,
      MultipartFile file,
      String facultyEmail
  ) throws IOException {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }

    Staff staff = staffProfileService.ensureForUser(user);

    String normalizedClass = normalize(className);
    if (normalizedClass == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Class name is required");
    }

    assertClassAccess(staff, normalizedClass);

    String normalizedDepartment = normalize(department);
    if (normalizedDepartment == null && normalizedClass != null) {
      normalizedDepartment = extractDepartment(normalizedClass);
    }

    LocalDateTime now = LocalDateTime.now();
    CourseAssignment assignment = new CourseAssignment(
        staff,
        title,
        description,
        dueDate,
        normalizedDepartment,
        normalizedClass,
        file != null ? file.getOriginalFilename() : null,
        file != null ? file.getContentType() : null,
        file != null ? file.getBytes() : null,
        now,
        true,
        now
    );

    CourseAssignment saved = assignmentRepository.save(assignment);
    return toResponse(saved);
  }

  public List<CourseAssignmentResponse> getAssignments(String email) {
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    if (user.getRole() == Role.STUDENT) {
      Student student = studentProfileService.ensureForUser(user);
      String className = normalize(buildClassKey(student.getDepartment(), student.getSection()));
      List<CourseAssignmentResponse> assignments = assignmentRepository.findByClassNameAndIsVisibleTrueOrderByCreatedAtDesc(className)
          .stream()
          .map(this::toResponse)
          .toList();
      return assignments.isEmpty() ? dataGenerator.getDefaultAssignments() : assignments;
    }

    if (user.getRole() == Role.STAFF) {
      Staff staff = staffProfileService.ensureForUser(user);
      List<String> assignedClasses = getAssignedClasses(staff);
      if (assignedClasses.isEmpty()) {
        return dataGenerator.getDefaultAssignments();
      }
      List<CourseAssignmentResponse> assignments = assignmentRepository.findByCreatedByIdAndClassNameInOrderByCreatedAtDesc(staff.getId(), assignedClasses)
          .stream()
          .map(this::toResponse)
          .toList();
      return assignments.isEmpty() ? dataGenerator.getDefaultAssignments() : assignments;
    }

    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
  }

  public CourseAssignment getAssignment(Long id) {
    return assignmentRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Assignment not found"));
  }

  public CourseAssignmentResponse updateAssignment(
      Long id,
      String title,
      String description,
      LocalDate dueDate,
      String department,
      String className,
      MultipartFile file,
      String facultyEmail
  ) throws IOException {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }

    Staff staff = staffProfileService.ensureForUser(user);
    CourseAssignment assignment = getAssignment(id);
    if (!assignment.getCreatedBy().getId().equals(staff.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty can edit only their assignments");
    }
    assertClassAccess(staff, assignment.getClassName());

    String normalizedClass = normalize(className);
    if (normalizedClass != null) {
      assertClassAccess(staff, normalizedClass);
      assignment.setClassName(normalizedClass);
    }

    if (title != null && !title.isBlank()) {
      assignment.setTitle(title);
    }
    if (description != null) {
      assignment.setDescription(description);
    }
    if (dueDate != null) {
      assignment.setDueDate(dueDate);
    }
    if (department != null) {
      assignment.setDepartment(normalize(department));
    } else if (normalizedClass != null && (assignment.getDepartment() == null || assignment.getDepartment().isBlank())) {
      assignment.setDepartment(extractDepartment(normalizedClass));
    }
    if (file != null && !file.isEmpty()) {
      assignment.setFileName(file.getOriginalFilename());
      assignment.setContentType(file.getContentType());
      assignment.setFileData(file.getBytes());
    }
    assignment.setUpdatedAt(LocalDateTime.now());

    CourseAssignment saved = assignmentRepository.save(assignment);
    return toResponse(saved);
  }

  public CourseAssignmentResponse updateVisibility(Long id, Boolean visible, String facultyEmail) {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }

    Staff staff = staffProfileService.ensureForUser(user);
    CourseAssignment assignment = getAssignment(id);
    if (!assignment.getCreatedBy().getId().equals(staff.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty can update only their assignments");
    }
    assertClassAccess(staff, assignment.getClassName());
    assertClassAccess(staff, assignment.getClassName());

    boolean nextValue = visible != null ? visible : !Boolean.TRUE.equals(assignment.getIsVisible());
    assignment.setIsVisible(nextValue);
    assignment.setUpdatedAt(LocalDateTime.now());
    return toResponse(assignmentRepository.save(assignment));
  }

  public void deleteAssignment(Long id, String facultyEmail) {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }

    Staff staff = staffProfileService.ensureForUser(user);
    CourseAssignment assignment = getAssignment(id);
    if (!assignment.getCreatedBy().getId().equals(staff.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty can delete only their assignments");
    }
    assertClassAccess(staff, assignment.getClassName());
    assignmentRepository.delete(assignment);
  }

  public AssignmentSubmissionResponse submitAssignment(
      Long assignmentId,
      String answerText,
      MultipartFile file,
      String studentEmail
  ) throws IOException {
    var user = userRepository.findByEmail(studentEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STUDENT) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
    }

    Student student = studentProfileService.ensureForUser(user);
    CourseAssignment assignment = getAssignment(assignmentId);

    String studentClass = normalize(buildClassKey(student.getDepartment(), student.getSection()));
    if (!assignment.getClassName().equalsIgnoreCase(studentClass)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Assignment not available for this student");
    }

    Optional<AssignmentSubmission> existing = submissionRepository.findByAssignmentIdAndStudentId(assignmentId, student.getId());
    AssignmentSubmission submission = existing.orElseGet(() -> new AssignmentSubmission(
        assignment,
        student,
        answerText,
        null,
        null,
        null,
        LocalDateTime.now()
    ));

    submission.setAnswerText(answerText);
    if (file != null && !file.isEmpty()) {
      submission.setFileName(file.getOriginalFilename());
      submission.setContentType(file.getContentType());
      submission.setFileData(file.getBytes());
    }
    submission.setSubmittedAt(LocalDateTime.now());

    AssignmentSubmission saved = submissionRepository.save(submission);
    return toSubmissionResponse(saved);
  }

  public List<AssignmentSubmissionResponse> getSubmissions(Long assignmentId, String facultyEmail) {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }

    Staff staff = staffProfileService.ensureForUser(user);
    CourseAssignment assignment = getAssignment(assignmentId);

    if (!assignment.getCreatedBy().getId().equals(staff.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty can view only their assignments");
    }
    assertClassAccess(staff, assignment.getClassName());

    return submissionRepository.findByAssignmentId(assignmentId)
        .stream()
        .map(this::toSubmissionResponse)
        .toList();
  }

  public List<AssignmentSubmissionResponse> getStudentSubmissions(String email) {
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STUDENT) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
    }
    Student student = studentProfileService.ensureForUser(user);
    return submissionRepository.findByStudentId(student.getId())
        .stream()
        .map(this::toSubmissionResponse)
        .toList();
  }

  public AssignmentSubmissionResponse gradeSubmission(Long submissionId, AssignmentGradeRequest request, String facultyEmail) {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }

    Staff staff = staffProfileService.ensureForUser(user);
    AssignmentSubmission submission = submissionRepository.findById(submissionId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));

    if (!submission.getAssignment().getCreatedBy().getId().equals(staff.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty can grade only their assignments");
    }
    assertClassAccess(staff, submission.getAssignment().getClassName());

    submission.setMarks(request.getMarks());
    submission.setFeedback(request.getFeedback());
    submission.setGradedAt(LocalDateTime.now());
    return toSubmissionResponse(submissionRepository.save(submission));
  }

  public AssignmentSubmission getSubmissionForDownload(Long submissionId, String requesterEmail) {
    var user = userRepository.findByEmail(requesterEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    AssignmentSubmission submission = submissionRepository.findById(submissionId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));

    if (user.getRole() == Role.STUDENT) {
      Student student = studentProfileService.ensureForUser(user);
      if (!submission.getStudent().getId().equals(student.getId())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
      }
      return submission;
    }

    if (user.getRole() == Role.STAFF) {
      Staff staff = staffProfileService.ensureForUser(user);
      if (!submission.getAssignment().getCreatedBy().getId().equals(staff.getId())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
      }
      assertClassAccess(staff, submission.getAssignment().getClassName());
      return submission;
    }

    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
  }

  public CourseAssignment getAssignmentForDownload(Long assignmentId, String requesterEmail) {
    var user = userRepository.findByEmail(requesterEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    CourseAssignment assignment = getAssignment(assignmentId);

    if (user.getRole() == Role.STUDENT) {
      Student student = studentProfileService.ensureForUser(user);
      String studentClass = normalize(buildClassKey(student.getDepartment(), student.getSection()));
      if (studentClass == null || assignment.getClassName() == null
          || !assignment.getClassName().equalsIgnoreCase(studentClass)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
      }
      if (!Boolean.TRUE.equals(assignment.getIsVisible())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
      }
      return assignment;
    }

    if (user.getRole() == Role.STAFF) {
      Staff staff = staffProfileService.ensureForUser(user);
      if (!assignment.getCreatedBy().getId().equals(staff.getId())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
      }
      assertClassAccess(staff, assignment.getClassName());
      return assignment;
    }

    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
  }

  private CourseAssignmentResponse toResponse(CourseAssignment assignment) {
    return new CourseAssignmentResponse(
        assignment.getId(),
        assignment.getTitle(),
        assignment.getDescription(),
        assignment.getDueDate(),
        assignment.getDepartment(),
        assignment.getClassName(),
        assignment.getCreatedBy().getUser().getFullName(),
        assignment.getCreatedAt(),
        assignment.getUpdatedAt(),
        assignment.getFileData() != null,
        Boolean.TRUE.equals(assignment.getIsVisible())
    );
  }

  private AssignmentSubmissionResponse toSubmissionResponse(AssignmentSubmission submission) {
    return new AssignmentSubmissionResponse(
        submission.getId(),
        submission.getAssignment().getId(),
        submission.getStudent().getId(),
        submission.getStudent().getUser().getFullName(),
        submission.getStudent().getStudentCode(),
        submission.getAnswerText(),
        submission.getMarks(),
        submission.getFeedback(),
        submission.getSubmittedAt(),
        submission.getGradedAt(),
        submission.getFileData() != null
    );
  }

  private String buildClassKey(String department, String section) {
    if (department == null || section == null) {
      return null;
    }
    return department.trim() + "-" + section.trim();
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      return null;
    }
    return trimmed.toUpperCase(Locale.ROOT);
  }

  private String extractDepartment(String className) {
    if (className == null) {
      return null;
    }
    int dashIndex = className.lastIndexOf("-");
    if (dashIndex > 0) {
      return normalize(className.substring(0, dashIndex));
    }
    return normalize(className);
  }

  private List<String> getAssignedClasses(Staff staff) {
    List<String> mapped = mappingRepository.findByStaffId(staff.getId()).stream()
        .map(mapping -> normalize(mapping.getClassName()))
        .filter(Objects::nonNull)
        .toList();
    if (!mapped.isEmpty()) {
      return mapped;
    }
    if (staff.getAssignedClasses() == null || staff.getAssignedClasses().isBlank()) {
      return List.of();
    }
    return Arrays.stream(staff.getAssignedClasses().split(","))
        .map(this::normalize)
        .filter(Objects::nonNull)
        .toList();
  }

  private void assertClassAccess(Staff staff, String className) {
    String normalized = normalize(className);
    if (normalized == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Class name is required");
    }
    boolean allowed = getAssignedClasses(staff).stream()
        .anyMatch(value -> value.equalsIgnoreCase(normalized));
    if (!allowed) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty is not assigned to this class");
    }
  }
}
