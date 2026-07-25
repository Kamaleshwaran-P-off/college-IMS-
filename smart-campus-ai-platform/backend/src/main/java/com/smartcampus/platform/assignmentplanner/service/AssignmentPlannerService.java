package com.smartcampus.platform.assignmentplanner.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.assignmentplanner.dto.PlannerAssignmentRequest;
import com.smartcampus.platform.assignmentplanner.dto.PlannerAssignmentResponse;
import com.smartcampus.platform.assignmentplanner.entity.AssignmentTargetType;
import com.smartcampus.platform.assignmentplanner.entity.PlannerAssignment;
import com.smartcampus.platform.assignmentplanner.repository.PlannerAssignmentRepository;
import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.defaultdata.RealisticDataGenerator;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.staff.service.StaffProfileService;
import com.smartcampus.platform.student.entity.Student;
import com.smartcampus.platform.student.service.StudentProfileService;

@Service
@Transactional
public class AssignmentPlannerService {
  private final PlannerAssignmentRepository assignmentRepository;
  private final StaffProfileService staffProfileService;
  private final StudentProfileService studentProfileService;
  private final RealisticDataGenerator dataGenerator;

  public AssignmentPlannerService(
      PlannerAssignmentRepository assignmentRepository,
      StaffProfileService staffProfileService,
      StudentProfileService studentProfileService,
      RealisticDataGenerator dataGenerator
  ) {
    this.assignmentRepository = assignmentRepository;
    this.staffProfileService = staffProfileService;
    this.studentProfileService = studentProfileService;
    this.dataGenerator = dataGenerator;
  }

  public PlannerAssignmentResponse createAssignment(PlannerAssignmentRequest request, User user) {
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }
    Staff staff = staffProfileService.ensureForUser(user);

    validateRequest(request);

    PlannerAssignment assignment = new PlannerAssignment(
        request.getTitle().trim(),
        request.getDescription(),
        request.getDeadline(),
        normalizeHours(request.getEstimatedHours()),
        request.getTargetType(),
        request.getTargetStudentId(),
        normalizeValue(request.getTargetDepartment()),
        normalizeValue(request.getTargetSection()),
        staff,
        LocalDateTime.now()
    );

    return toResponse(assignmentRepository.save(assignment));
  }

  public List<PlannerAssignmentResponse> getAssignmentsForStudent(User user) {
    if (user.getRole() != Role.STUDENT) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
    }
    Student student = studentProfileService.ensureForUser(user);
    List<PlannerAssignment> assignments = fetchAssignmentsForStudent(student);
    if (assignments.isEmpty()) {
      return dataGenerator.getDefaultPlannerAssignments();
    }
    return assignments.stream()
        .sorted(Comparator.comparing(PlannerAssignment::getDeadline, Comparator.nullsLast(Comparator.naturalOrder())))
        .map(this::toResponse)
        .toList();
  }

  public List<PlannerAssignmentResponse> getAssignmentsForFaculty(User user) {
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }
    Staff staff = staffProfileService.ensureForUser(user);
    List<PlannerAssignmentResponse> assignments = assignmentRepository.findByCreatedById(staff.getId()).stream()
        .sorted(Comparator.comparing(PlannerAssignment::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
        .map(this::toResponse)
        .toList();
    return assignments.isEmpty() ? dataGenerator.getDefaultPlannerAssignments() : assignments;
  }

  public List<PlannerAssignment> fetchAssignmentsForStudent(Student student) {
    List<PlannerAssignment> assignments = new ArrayList<>();
    assignments.addAll(assignmentRepository.findByTargetStudentId(student.getId()));

    String department = normalizeValue(student.getDepartment());
    String section = normalizeValue(student.getSection());
    if (department != null && section != null) {
      assignments.addAll(assignmentRepository.findByTargetDepartmentAndTargetSection(department, section));
    }
    return assignments.stream().distinct().collect(Collectors.toList());
  }

  private void validateRequest(PlannerAssignmentRequest request) {
    if (request.getTargetType() == AssignmentTargetType.STUDENT) {
      if (request.getTargetStudentId() == null) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Target student is required.");
      }
    } else if (request.getTargetType() == AssignmentTargetType.CLASS) {
      if (isBlank(request.getTargetDepartment()) || isBlank(request.getTargetSection())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department and section are required.");
      }
    }
  }

  private Double normalizeHours(Double hours) {
    if (hours == null || hours <= 0) {
      return 2.0;
    }
    return Math.round(hours * 10.0) / 10.0;
  }

  private String normalizeValue(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      return null;
    }
    return trimmed.toUpperCase(Locale.ROOT);
  }

  private boolean isBlank(String value) {
    return value == null || value.trim().isEmpty();
  }

  private PlannerAssignmentResponse toResponse(PlannerAssignment assignment) {
    String createdByName = assignment.getCreatedBy() != null && assignment.getCreatedBy().getUser() != null
        ? assignment.getCreatedBy().getUser().getFullName()
        : "Faculty";
    return new PlannerAssignmentResponse(
        assignment.getId(),
        assignment.getTitle(),
        assignment.getDescription(),
        assignment.getDeadline(),
        assignment.getEstimatedHours(),
        assignment.getTargetType(),
        assignment.getTargetStudentId(),
        assignment.getTargetDepartment(),
        assignment.getTargetSection(),
        createdByName,
        assignment.getCreatedAt()
    );
  }
}
