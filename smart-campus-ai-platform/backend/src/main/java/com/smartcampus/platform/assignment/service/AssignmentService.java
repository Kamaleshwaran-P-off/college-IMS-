package com.smartcampus.platform.assignment.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartcampus.platform.assignment.dto.AssignmentRequest;
import com.smartcampus.platform.assignment.dto.AssignmentResponse;
import com.smartcampus.platform.assignment.entity.Assignment;
import com.smartcampus.platform.assignment.repository.AssignmentRepository;
import com.smartcampus.platform.common.exception.ResourceNotFoundException;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.staff.repository.StaffRepository;
import com.smartcampus.platform.student.entity.Student;
import com.smartcampus.platform.student.repository.StudentRepository;
import com.smartcampus.platform.notification.entity.NotificationType;
import com.smartcampus.platform.notification.service.NotificationService;

@Service
@Transactional
public class AssignmentService {
  private final AssignmentRepository assignmentRepository;
  private final StaffRepository staffRepository;
  private final StudentRepository studentRepository;
  private final NotificationService notificationService;

  public AssignmentService(
      AssignmentRepository assignmentRepository,
      StaffRepository staffRepository,
      StudentRepository studentRepository,
      NotificationService notificationService
  ) {
    this.assignmentRepository = assignmentRepository;
    this.staffRepository = staffRepository;
    this.studentRepository = studentRepository;
    this.notificationService = notificationService;
  }

  public AssignmentResponse create(AssignmentRequest request) {
    Staff staff = staffRepository.findById(request.getStaffId())
        .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
    Student student = studentRepository.findById(request.getStudentId())
        .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

    Assignment assignment = new Assignment(
        staff,
        student,
        request.getTitle(),
        request.getDescription(),
        request.getDueDate(),
        request.getCourseCode()
    );

    Assignment saved = assignmentRepository.save(assignment);

    String message = "New assignment: " + saved.getTitle();
    if (saved.getDueDate() != null) {
      message += " (Due " + saved.getDueDate() + ")";
    }
    notificationService.createNotification(
        saved.getAssignedStudent().getUser().getId(),
        NotificationType.ASSIGNMENT,
        "Assignment uploaded",
        message
    );

    return toResponse(saved);
  }

  public List<AssignmentResponse> findAll() {
    return assignmentRepository.findAll().stream().map(this::toResponse).toList();
  }

  public AssignmentResponse findById(Long id) {
    return toResponse(getAssignment(id));
  }

  public AssignmentResponse update(Long id, AssignmentRequest request) {
    Assignment assignment = getAssignment(id);
    Staff staff = staffRepository.findById(request.getStaffId())
        .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
    Student student = studentRepository.findById(request.getStudentId())
        .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

    assignment.setCreatedBy(staff);
    assignment.setAssignedStudent(student);
    assignment.setTitle(request.getTitle());
    assignment.setDescription(request.getDescription());
    assignment.setDueDate(request.getDueDate());
    assignment.setCourseCode(request.getCourseCode());

    Assignment saved = assignmentRepository.save(assignment);
    return toResponse(saved);
  }

  public void delete(Long id) {
    Assignment assignment = getAssignment(id);
    assignmentRepository.delete(assignment);
  }

  private Assignment getAssignment(Long id) {
    return assignmentRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
  }

  private AssignmentResponse toResponse(Assignment assignment) {
    return new AssignmentResponse(
        assignment.getId(),
        assignment.getCreatedBy().getId(),
        assignment.getAssignedStudent().getId(),
        assignment.getTitle(),
        assignment.getDescription(),
        assignment.getDueDate(),
        assignment.getCourseCode()
    );
  }
}
