package com.smartcampus.platform.marks.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.marks.dto.MarksRequest;
import com.smartcampus.platform.marks.dto.MarksResponse;
import com.smartcampus.platform.marks.entity.StudentMarks;
import com.smartcampus.platform.marks.repository.StudentMarksRepository;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.staff.repository.StaffRepository;
import com.smartcampus.platform.staff.service.FacultyClassAssignmentService;
import com.smartcampus.platform.staff.service.FacultySubjectAssignmentService;
import com.smartcampus.platform.student.entity.Student;
import com.smartcampus.platform.student.repository.StudentRepository;

@Service
@Transactional
public class MarksService {
  private final UserRepository userRepository;
  private final StudentRepository studentRepository;
  private final StaffRepository staffRepository;
  private final StudentMarksRepository marksRepository;
  private final FacultySubjectAssignmentService subjectAssignmentService;
  private final FacultyClassAssignmentService classAssignmentService;

  public MarksService(
      UserRepository userRepository,
      StudentRepository studentRepository,
      StaffRepository staffRepository,
      StudentMarksRepository marksRepository,
      FacultySubjectAssignmentService subjectAssignmentService,
      FacultyClassAssignmentService classAssignmentService
  ) {
    this.userRepository = userRepository;
    this.studentRepository = studentRepository;
    this.staffRepository = staffRepository;
    this.marksRepository = marksRepository;
    this.subjectAssignmentService = subjectAssignmentService;
    this.classAssignmentService = classAssignmentService;
  }

  public MarksResponse upsertMarks(MarksRequest request, String facultyEmail) {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }

    Staff staff = staffRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty profile not found"));

    Student student = studentRepository.findById(request.getStudentId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

    String subject = normalize(request.getSubject());
    String department = normalize(student.getDepartment());
    String section = normalize(student.getSection());
    if (!classAssignmentService.hasClassAccess(staff.getId(), department, section)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty is not assigned to this class");
    }
    if (subjectAssignmentService.hasAnyAssignment(staff.getId())
        && !subjectAssignmentService.hasFacultyAccess(staff.getId(), subject, department, section)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty is not assigned to this subject/class");
    }

    Optional<StudentMarks> existing = marksRepository.findByStudentIdAndCourseCodeIgnoreCase(student.getId(), subject)
        .stream()
        .findFirst();

    StudentMarks marks = existing.orElseGet(() -> new StudentMarks(
        student,
        staff,
        subject,
        null,
        null,
        null,
        null,
        LocalDateTime.now()
    ));

    marks.setRecordedBy(staff);
    marks.setCourseCode(subject);
    marks.setCat1(request.getCat1());
    marks.setCat2(request.getCat2());
    marks.setCat3(request.getCat3());
    marks.setAssignmentScore(request.getAssignmentScore());
    marks.setUpdatedAt(LocalDateTime.now());

    return toResponse(marksRepository.save(marks));
  }

  public List<MarksResponse> getMarksForStudent(String studentEmail) {
    var user = userRepository.findByEmail(studentEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STUDENT) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
    }

    Student student = studentRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

    return marksRepository.findByStudentId(student.getId())
        .stream()
        .map(this::toResponse)
        .toList();
  }

  public List<MarksResponse> getMarksForClass(String facultyEmail, String subject, String department, String section) {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }

    Staff staff = staffRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty profile not found"));

    String normalizedSubject = normalize(subject);
    String normalizedDepartment = normalize(department);
    String normalizedSection = normalize(section);

    if (!classAssignmentService.hasClassAccess(staff.getId(), normalizedDepartment, normalizedSection)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty is not assigned to this class");
    }
    if (subjectAssignmentService.hasAnyAssignment(staff.getId())
        && !subjectAssignmentService.hasFacultyAccess(staff.getId(), normalizedSubject, normalizedDepartment, normalizedSection)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty is not assigned to this subject/class");
    }

    List<Student> students = studentRepository.findByDepartmentIgnoreCaseAndSectionIgnoreCase(
        normalizedDepartment,
        normalizedSection
    );

    return students.stream()
        .map(student -> {
          Optional<StudentMarks> existing = marksRepository.findByStudentIdAndCourseCodeIgnoreCase(student.getId(), normalizedSubject)
              .stream()
              .findFirst();
          return existing.map(this::toResponse)
              .orElse(new MarksResponse(
                  null,
                  student.getId(),
                  student.getUser().getFullName(),
                  student.getStudentCode(),
                  normalizedSubject,
                  null,
                  null,
                  null,
                  null,
                  null
              ));
        })
        .toList();
  }

  private MarksResponse toResponse(StudentMarks marks) {
    return new MarksResponse(
        marks.getId(),
        marks.getStudent().getId(),
        marks.getStudent().getUser().getFullName(),
        marks.getStudent().getStudentCode(),
        marks.getCourseCode(),
        marks.getCat1(),
        marks.getCat2(),
        marks.getCat3(),
        marks.getAssignmentScore(),
        marks.getUpdatedAt()
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
