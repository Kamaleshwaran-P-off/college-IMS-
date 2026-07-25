package com.smartcampus.platform.attendance.service;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.smartcampus.platform.attendance.dto.AttendanceRequest;
import com.smartcampus.platform.attendance.dto.AttendanceResponse;
import com.smartcampus.platform.attendance.entity.Attendance;
import com.smartcampus.platform.attendance.repository.AttendanceRepository;
import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.common.exception.ResourceNotFoundException;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.staff.repository.StaffRepository;
import com.smartcampus.platform.staff.service.FacultyClassAssignmentService;
import com.smartcampus.platform.staff.service.FacultySubjectAssignmentService;
import com.smartcampus.platform.student.entity.Student;
import com.smartcampus.platform.student.repository.StudentRepository;

@Service
@Transactional
public class AttendanceService {
  private final AttendanceRepository attendanceRepository;
  private final StudentRepository studentRepository;
  private final StaffRepository staffRepository;
  private final UserRepository userRepository;
  private final FacultySubjectAssignmentService subjectAssignmentService;
  private final FacultyClassAssignmentService classAssignmentService;

  public AttendanceService(
      AttendanceRepository attendanceRepository,
      StudentRepository studentRepository,
      StaffRepository staffRepository,
      UserRepository userRepository,
      FacultySubjectAssignmentService subjectAssignmentService,
      FacultyClassAssignmentService classAssignmentService
  ) {
    this.attendanceRepository = attendanceRepository;
    this.studentRepository = studentRepository;
    this.staffRepository = staffRepository;
    this.userRepository = userRepository;
    this.subjectAssignmentService = subjectAssignmentService;
    this.classAssignmentService = classAssignmentService;
  }

  public AttendanceResponse create(AttendanceRequest request, String facultyEmail) {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }

    Staff staff = staffRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not found"));

    Student student = studentRepository.findById(request.getStudentId())
        .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

    String subject = normalize(request.getCourseCode());
    String department = normalize(student.getDepartment());
    String section = normalize(student.getSection());

    if (!classAssignmentService.hasClassAccess(staff.getId(), department, section)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty is not assigned to this class");
    }
    if (subjectAssignmentService.hasAnyAssignment(staff.getId())
        && !subjectAssignmentService.hasFacultyAccess(staff.getId(), subject, department, section)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty is not assigned to this subject/class");
    }

    if (attendanceRepository.existsByStudentIdAndClassDateAndCourseCodeIgnoreCase(
        student.getId(),
        request.getClassDate(),
        subject
    )) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Attendance already marked for this date");
    }

    Attendance attendance = new Attendance(
        student,
        staff,
        request.getClassDate(),
        request.getStatus(),
        subject
    );

    return toResponse(attendanceRepository.save(attendance));
  }

  public List<AttendanceResponse> findAll() {
    return attendanceRepository.findAll().stream().map(this::toResponse).toList();
  }

  public AttendanceResponse findById(Long id) {
    return toResponse(getAttendance(id));
  }

  public AttendanceResponse update(Long id, AttendanceRequest request, String facultyEmail) {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }
    Staff staff = staffRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not found"));

    Attendance attendance = getAttendance(id);

    Student student = studentRepository.findById(request.getStudentId())
        .orElseThrow(() -> new ResourceNotFoundException("Student not found"));

    String subject = normalize(request.getCourseCode());
    String department = normalize(student.getDepartment());
    String section = normalize(student.getSection());
    if (!classAssignmentService.hasClassAccess(staff.getId(), department, section)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty is not assigned to this class");
    }
    if (subjectAssignmentService.hasAnyAssignment(staff.getId())
        && !subjectAssignmentService.hasFacultyAccess(staff.getId(), subject, department, section)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty is not assigned to this subject/class");
    }

    attendance.setStudent(student);
    attendance.setRecordedBy(staff);
    attendance.setClassDate(request.getClassDate());
    attendance.setStatus(request.getStatus());
    attendance.setCourseCode(subject);

    return toResponse(attendanceRepository.save(attendance));
  }

  public List<AttendanceResponse> getStudentAttendance(String studentEmail, String subject) {
    var user = userRepository.findByEmail(studentEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STUDENT) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
    }
    Student student = studentRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
    if (subject == null || subject.isBlank()) {
      return attendanceRepository.findByStudentIdAndClassDateBetween(
          student.getId(),
          java.time.LocalDate.now().minusDays(30),
          java.time.LocalDate.now()
      ).stream().map(this::toResponse).toList();
    }
    return attendanceRepository.findByStudentIdAndCourseCodeIgnoreCase(student.getId(), subject)
        .stream()
        .map(this::toResponse)
        .toList();
  }

  public List<AttendanceResponse> getClassAttendance(
      String facultyEmail,
      String subject,
      String department,
      String section,
      java.time.LocalDate startDate,
      java.time.LocalDate endDate
  ) {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }

    Staff staff = staffRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not found"));

    String normalizedSubject = normalize(subject);
    String normalizedDepartment = normalize(department);
    String normalizedSection = normalize(section);

    if (normalizedSubject == null || normalizedDepartment == null || normalizedSection == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject, department, and section are required");
    }

    if (!classAssignmentService.hasClassAccess(staff.getId(), normalizedDepartment, normalizedSection)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty is not assigned to this class");
    }
    if (subjectAssignmentService.hasAnyAssignment(staff.getId())
        && !subjectAssignmentService.hasFacultyAccess(staff.getId(), normalizedSubject, normalizedDepartment, normalizedSection)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty is not assigned to this subject/class");
    }

    List<Attendance> records;
    if (startDate != null && endDate != null) {
      records = attendanceRepository
          .findByStudentDepartmentIgnoreCaseAndStudentSectionIgnoreCaseAndCourseCodeIgnoreCaseAndClassDateBetween(
              normalizedDepartment,
              normalizedSection,
              normalizedSubject,
              startDate,
              endDate
          );
    } else {
      records = attendanceRepository
          .findByStudentDepartmentIgnoreCaseAndStudentSectionIgnoreCaseAndCourseCodeIgnoreCase(
              normalizedDepartment,
              normalizedSection,
              normalizedSubject
          );
    }

    // Ensure deterministic ordering by date then student.
    Map<Long, List<Attendance>> grouped = records.stream()
        .collect(Collectors.groupingBy(attendance -> attendance.getStudent().getId()));

    return grouped.values()
        .stream()
        .flatMap(list -> list.stream())
        .sorted((a, b) -> {
          int dateCompare = a.getClassDate().compareTo(b.getClassDate());
          if (dateCompare != 0) return dateCompare;
          return a.getStudent().getId().compareTo(b.getStudent().getId());
        })
        .map(this::toResponse)
        .toList();
  }

  public void delete(Long id) {
    Attendance attendance = getAttendance(id);
    attendanceRepository.delete(attendance);
  }

  private Attendance getAttendance(Long id) {
    return attendanceRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Attendance record not found"));
  }

  private AttendanceResponse toResponse(Attendance attendance) {
    Long staffId = attendance.getRecordedBy() != null ? attendance.getRecordedBy().getId() : null;
    return new AttendanceResponse(
        attendance.getId(),
        attendance.getStudent().getId(),
        staffId,
        attendance.getClassDate(),
        attendance.getStatus(),
        attendance.getCourseCode()
    );
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
}
