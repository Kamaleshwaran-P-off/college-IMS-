package com.smartcampus.platform.reports.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.attendance.entity.Attendance;
import com.smartcampus.platform.attendance.entity.AttendanceStatus;
import com.smartcampus.platform.attendance.repository.AttendanceRepository;
import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.marks.entity.StudentMarks;
import com.smartcampus.platform.marks.repository.StudentMarksRepository;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.staff.repository.StaffRepository;
import com.smartcampus.platform.staff.service.FacultySubjectAssignmentService;
import com.smartcampus.platform.student.entity.Student;
import com.smartcampus.platform.student.repository.StudentRepository;

@Service
@Transactional(readOnly = true)
public class GradeExportService {
  private final UserRepository userRepository;
  private final StaffRepository staffRepository;
  private final StudentRepository studentRepository;
  private final StudentMarksRepository marksRepository;
  private final AttendanceRepository attendanceRepository;
  private final FacultySubjectAssignmentService subjectAssignmentService;

  public GradeExportService(
      UserRepository userRepository,
      StaffRepository staffRepository,
      StudentRepository studentRepository,
      StudentMarksRepository marksRepository,
      AttendanceRepository attendanceRepository,
      FacultySubjectAssignmentService subjectAssignmentService
  ) {
    this.userRepository = userRepository;
    this.staffRepository = staffRepository;
    this.studentRepository = studentRepository;
    this.marksRepository = marksRepository;
    this.attendanceRepository = attendanceRepository;
    this.subjectAssignmentService = subjectAssignmentService;
  }

  public byte[] exportGrades(
      String facultyEmail,
      String subject,
      String department,
      String section,
      LocalDate startDate,
      LocalDate endDate
  ) {
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

    if (normalizedSubject == null || normalizedDepartment == null || normalizedSection == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subject, department, and section are required");
    }

    if (!subjectAssignmentService.hasFacultyAccess(staff.getId(), normalizedSubject, normalizedDepartment, normalizedSection)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty is not assigned to this subject/class");
    }

    List<Student> students = studentRepository.findByDepartmentIgnoreCaseAndSectionIgnoreCase(
        normalizedDepartment,
        normalizedSection
    );

    List<Attendance> attendanceRecords = fetchAttendance(
        normalizedDepartment,
        normalizedSection,
        normalizedSubject,
        startDate,
        endDate
    );

    Map<Long, List<Attendance>> attendanceByStudent = attendanceRecords.stream()
        .collect(Collectors.groupingBy(attendance -> attendance.getStudent().getId()));

    StringBuilder csv = new StringBuilder();
    csv.append("Register No,Student Name,Subject,CAT1,CAT2,CAT3,Assignment,Attendance %\n");

    students.stream()
        .sorted(Comparator.comparing(Student::getStudentCode, Comparator.nullsLast(String::compareToIgnoreCase)))
        .forEach(student -> {
          Optional<StudentMarks> marks = marksRepository
              .findByStudentIdAndCourseCodeIgnoreCase(student.getId(), normalizedSubject)
              .stream()
              .findFirst();

          double cat1 = marks.map(StudentMarks::getCat1).orElse(0.0);
          double cat2 = marks.map(StudentMarks::getCat2).orElse(0.0);
          double cat3 = marks.map(StudentMarks::getCat3).orElse(0.0);
          double assignmentScore = marks.map(StudentMarks::getAssignmentScore).orElse(0.0);

          List<Attendance> records = attendanceByStudent.getOrDefault(student.getId(), List.of());
          long total = records.size();
          long present = records.stream().filter(r -> r.getStatus() == AttendanceStatus.PRESENT).count();
          double attendancePercent = total == 0 ? 0.0 : (present * 100.0) / total;

          csv.append(escape(student.getStudentCode()))
              .append(',')
              .append(escape(student.getUser().getFullName()))
              .append(',')
              .append(escape(normalizedSubject))
              .append(',')
              .append(formatNumber(cat1))
              .append(',')
              .append(formatNumber(cat2))
              .append(',')
              .append(formatNumber(cat3))
              .append(',')
              .append(formatNumber(assignmentScore))
              .append(',')
              .append(String.format(Locale.ROOT, "%.2f", attendancePercent))
              .append('\n');
        });

    return csv.toString().getBytes(StandardCharsets.UTF_8);
  }

  private List<Attendance> fetchAttendance(
      String department,
      String section,
      String subject,
      LocalDate startDate,
      LocalDate endDate
  ) {
    if (startDate != null && endDate != null) {
      return attendanceRepository
          .findByStudentDepartmentIgnoreCaseAndStudentSectionIgnoreCaseAndCourseCodeIgnoreCaseAndClassDateBetween(
              department,
              section,
              subject,
              startDate,
              endDate
          );
    }
    return attendanceRepository
        .findByStudentDepartmentIgnoreCaseAndStudentSectionIgnoreCaseAndCourseCodeIgnoreCase(
            department,
            section,
            subject
        );
  }

  private String formatNumber(Double value) {
    if (value == null) {
      return "0";
    }
    return String.format(Locale.ROOT, "%.2f", value);
  }

  private String escape(String value) {
    if (value == null) {
      return "";
    }
    String escaped = value.replace("\"", "\"\"");
    if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
      return "\"" + escaped + "\"";
    }
    return escaped;
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
