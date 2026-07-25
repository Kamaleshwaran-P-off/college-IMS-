package com.smartcampus.platform.dashboard.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.aiquiz.entity.AiQuizSubmission;
import com.smartcampus.platform.aiquiz.repository.AiQuizSubmissionRepository;
import com.smartcampus.platform.attendance.entity.Attendance;
import com.smartcampus.platform.attendance.entity.AttendanceStatus;
import com.smartcampus.platform.attendance.repository.AttendanceRepository;
import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.calendar.entity.AcademicCalendar;
import com.smartcampus.platform.calendar.repository.AcademicCalendarRepository;
import com.smartcampus.platform.dashboard.dto.FacultyDashboardResponse;
import com.smartcampus.platform.dashboard.dto.StudentDashboardResponse;
import com.smartcampus.platform.marks.entity.StudentMarks;
import com.smartcampus.platform.marks.repository.StudentMarksRepository;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.staff.repository.StaffRepository;
import com.smartcampus.platform.staff.service.StaffProfileService;
import com.smartcampus.platform.student.entity.Student;
import com.smartcampus.platform.student.repository.StudentRepository;
import com.smartcampus.platform.student.service.StudentProfileService;

@Service
public class DashboardService {
  private final StudentRepository studentRepository;
  private final StaffRepository staffRepository;
  private final StudentProfileService studentProfileService;
  private final StaffProfileService staffProfileService;
  private final AttendanceRepository attendanceRepository;
  private final StudentMarksRepository studentMarksRepository;
  private final AcademicCalendarRepository academicCalendarRepository;
  private final AiQuizSubmissionRepository aiQuizSubmissionRepository;
  private final DateTimeFormatter quizLabelFormatter = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);

  public DashboardService(
      StudentRepository studentRepository,
      StaffRepository staffRepository,
      AttendanceRepository attendanceRepository,
      StudentMarksRepository studentMarksRepository,
      AcademicCalendarRepository academicCalendarRepository,
      AiQuizSubmissionRepository aiQuizSubmissionRepository,
      StudentProfileService studentProfileService,
      StaffProfileService staffProfileService
  ) {
    this.studentRepository = studentRepository;
    this.staffRepository = staffRepository;
    this.attendanceRepository = attendanceRepository;
    this.studentMarksRepository = studentMarksRepository;
    this.academicCalendarRepository = academicCalendarRepository;
    this.aiQuizSubmissionRepository = aiQuizSubmissionRepository;
    this.studentProfileService = studentProfileService;
    this.staffProfileService = staffProfileService;
  }

  public StudentDashboardResponse buildStudentDashboard(User user) {
    if (user.getRole() != Role.STUDENT) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required.");
    }

    Student student = studentProfileService.ensureForUser(user);

    LocalDate today = LocalDate.now();
    LocalDate weekStart = today.minusDays(5);
    List<StudentDashboardResponse.ChartPoint> weekly = buildStudentWeeklyAttendance(student.getId(), weekStart, today);

    double overallPercent = calculateAttendancePercent(student.getId(), today.minusDays(30), today);

    StudentDashboardResponse.AttendanceSummary attendanceSummary =
        new StudentDashboardResponse.AttendanceSummary(overallPercent, weekly);

    List<StudentMarks> marks = studentMarksRepository.findByStudentId(student.getId());
    StudentDashboardResponse.ChartData assignmentMarks = buildAssignmentChart(marks);
    StudentDashboardResponse.InternalMarks internalMarks = buildInternalMarks(marks);
    StudentDashboardResponse.ChartData quizPerformance = buildQuizPerformance(student.getId());

    StudentDashboardResponse.CalendarInfo calendarInfo = buildCalendarInfo();

    List<StudentDashboardResponse.TimetableDay> timetable = buildDefaultTimetable(student.getDepartment(), student.getSection());

    double cgpa = marks.isEmpty() ? 8.2 : calculateCgpa(marks);

    return new StudentDashboardResponse(
        cgpa,
        attendanceSummary,
        assignmentMarks,
        internalMarks,
        quizPerformance,
        calendarInfo,
        timetable
    );
  }

  public FacultyDashboardResponse buildFacultyDashboard(User user) {
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required.");
    }

    Staff staff = staffProfileService.ensureForUser(user);

    LocalDate today = LocalDate.now();
    LocalDate start = today.minusDays(4);

    FacultyDashboardResponse.AttendanceSeries attendanceSeries = buildFacultyAttendance(staff, start, today);

    FacultyDashboardResponse.InternalMarks internalMarks = buildFacultyInternalMarks();
    FacultyDashboardResponse.ChartData assignmentMarks = buildFacultyAssignmentMarks();

    List<FacultyDashboardResponse.TodayClass> todayClasses = buildTodayClasses(staff);

    return new FacultyDashboardResponse(attendanceSeries, internalMarks, assignmentMarks, todayClasses);
  }

  private List<StudentDashboardResponse.ChartPoint> buildStudentWeeklyAttendance(Long studentId, LocalDate start, LocalDate end) {
    List<Attendance> records = attendanceRepository.findByStudentIdAndClassDateBetween(studentId, start, end);

    Map<LocalDate, List<Attendance>> grouped = records.stream()
        .collect(Collectors.groupingBy(Attendance::getClassDate));

    List<StudentDashboardResponse.ChartPoint> points = new ArrayList<>();
    for (int i = 0; i <= 5; i++) {
      LocalDate date = start.plusDays(i);
      List<Attendance> dayRecords = grouped.getOrDefault(date, List.of());
      double percent = calculatePercent(dayRecords);
      points.add(new StudentDashboardResponse.ChartPoint(dayLabel(date), percent));
    }

    return points;
  }

  private double calculateAttendancePercent(Long studentId, LocalDate start, LocalDate end) {
    long total = attendanceRepository.countByStudentIdAndClassDateBetween(studentId, start, end);
    if (total == 0) {
      return 0;
    }
    long present = attendanceRepository.countByStudentIdAndClassDateBetweenAndStatus(studentId, start, end, AttendanceStatus.PRESENT);
    return Math.round((present * 100.0 / total) * 10.0) / 10.0;
  }

  private double calculatePercent(List<Attendance> records) {
    if (records.isEmpty()) {
      return 0;
    }
    long present = records.stream().filter(record -> record.getStatus() == AttendanceStatus.PRESENT).count();
    return Math.round((present * 100.0 / records.size()) * 10.0) / 10.0;
  }

  private StudentDashboardResponse.ChartData buildAssignmentChart(List<StudentMarks> marks) {
    if (marks.isEmpty()) {
      return new StudentDashboardResponse.ChartData(
          List.of("AI Systems", "DSA", "Math", "Networks"),
          List.of(86.0, 78.0, 91.0, 83.0)
      );
    }

    List<String> labels = marks.stream().map(StudentMarks::getCourseCode).toList();
    List<Double> scores = marks.stream()
        .map(mark -> Optional.ofNullable(mark.getAssignmentScore()).orElse(0.0))
        .toList();

    return new StudentDashboardResponse.ChartData(labels, scores);
  }

  private StudentDashboardResponse.ChartData buildQuizPerformance(Long studentId) {
    List<AiQuizSubmission> submissions = aiQuizSubmissionRepository.findByStudentId(studentId);
    if (submissions.isEmpty()) {
      return new StudentDashboardResponse.ChartData(
          List.of("Week 1", "Week 2", "Week 3", "Week 4"),
          List.of(62.0, 70.0, 78.0, 86.0)
      );
    }

    List<AiQuizSubmission> sorted = submissions.stream()
        .filter(submission -> submission.getSubmittedAt() != null)
        .sorted(Comparator.comparing(AiQuizSubmission::getSubmittedAt))
        .toList();

    int startIndex = Math.max(0, sorted.size() - 6);
    List<AiQuizSubmission> recent = sorted.subList(startIndex, sorted.size());

    List<String> labels = recent.stream()
        .map(submission -> submission.getSubmittedAt().format(quizLabelFormatter))
        .toList();
    List<Double> scores = recent.stream()
        .map(submission -> {
          if (submission.getTotal() == null || submission.getTotal() == 0 || submission.getScore() == null) {
            return 0.0;
          }
          return Math.round((submission.getScore() * 100.0 / submission.getTotal()) * 10.0) / 10.0;
        })
        .toList();

    return new StudentDashboardResponse.ChartData(labels, scores);
  }

  private StudentDashboardResponse.InternalMarks buildInternalMarks(List<StudentMarks> marks) {
    if (marks.isEmpty()) {
      return new StudentDashboardResponse.InternalMarks(
          List.of("AI Systems", "DSA", "Math", "Networks"),
          List.of(78.0, 72.0, 84.0, 75.0),
          List.of(82.0, 76.0, 88.0, 79.0),
          List.of(85.0, 80.0, 90.0, 82.0)
      );
    }

    List<String> labels = marks.stream().map(StudentMarks::getCourseCode).toList();
    List<Double> cat1 = marks.stream().map(mark -> Optional.ofNullable(mark.getCat1()).orElse(0.0)).toList();
    List<Double> cat2 = marks.stream().map(mark -> Optional.ofNullable(mark.getCat2()).orElse(0.0)).toList();
    List<Double> cat3 = marks.stream().map(mark -> Optional.ofNullable(mark.getCat3()).orElse(0.0)).toList();

    return new StudentDashboardResponse.InternalMarks(labels, cat1, cat2, cat3);
  }

  private StudentDashboardResponse.CalendarInfo buildCalendarInfo() {
    Optional<AcademicCalendar> calendar = academicCalendarRepository.findTopByOrderByUploadedAtDesc();
    return calendar.map(value -> new StudentDashboardResponse.CalendarInfo(
        "/api/calendar/latest",
        value.getContentType(),
        value.getFileName()
    )).orElse(null);
  }

  private List<StudentDashboardResponse.TimetableDay> buildDefaultTimetable(String department, String section) {
    return List.of(
        new StudentDashboardResponse.TimetableDay(
            "Monday",
            List.of(
                new StudentDashboardResponse.TimetableSlot("09:00 - 09:50", "AI Systems", departmentRoom(department, "201")),
                new StudentDashboardResponse.TimetableSlot("10:00 - 10:50", "Math", departmentRoom(department, "103")),
                new StudentDashboardResponse.TimetableSlot("11:00 - 11:50", "DSA", departmentRoom(department, "302"))
            )
        ),
        new StudentDashboardResponse.TimetableDay(
            "Tuesday",
            List.of(
                new StudentDashboardResponse.TimetableSlot("09:00 - 09:50", "Networks", departmentRoom(department, "305")),
                new StudentDashboardResponse.TimetableSlot("10:00 - 10:50", "Math", departmentRoom(department, "103")),
                new StudentDashboardResponse.TimetableSlot("11:00 - 11:50", "AI Lab", "Lab-2")
            )
        )
    );
  }

  private FacultyDashboardResponse.AttendanceSeries buildFacultyAttendance(Staff staff, LocalDate start, LocalDate end) {
    List<Attendance> records = attendanceRepository.findByRecordedByIdAndClassDateBetween(staff.getId(), start, end);
    Map<LocalDate, List<Attendance>> grouped = records.stream()
        .collect(Collectors.groupingBy(Attendance::getClassDate));

    List<String> labels = new ArrayList<>();
    List<Double> values = new ArrayList<>();
    for (int i = 0; i <= 4; i++) {
      LocalDate date = start.plusDays(i);
      labels.add(dayLabel(date));
      List<Attendance> dayRecords = grouped.getOrDefault(date, List.of());
      values.add(calculatePercent(dayRecords));
    }

    return new FacultyDashboardResponse.AttendanceSeries(
        labels,
        List.of(new FacultyDashboardResponse.Series("All Classes", values))
    );
  }

  private FacultyDashboardResponse.InternalMarks buildFacultyInternalMarks() {
    return new FacultyDashboardResponse.InternalMarks(
        List.of("AI Systems", "DSA", "Math", "Networks"),
        List.of(75.0, 72.0, 78.0, 70.0),
        List.of(79.0, 76.0, 80.0, 74.0),
        List.of(83.0, 81.0, 84.0, 78.0)
    );
  }

  private FacultyDashboardResponse.ChartData buildFacultyAssignmentMarks() {
    return new FacultyDashboardResponse.ChartData(
        List.of("AI Systems", "DSA", "Math", "Networks"),
        List.of(82.0, 78.0, 85.0, 80.0)
    );
  }

  private List<FacultyDashboardResponse.TodayClass> buildTodayClasses(Staff staff) {
    String classes = staff.getAssignedClasses() == null ? "" : staff.getAssignedClasses();
    List<String> classList = List.of(classes.split(",")).stream()
        .map(String::trim)
        .filter(value -> !value.isEmpty())
        .sorted(Comparator.naturalOrder())
        .toList();

    if (classList.isEmpty()) {
      return List.of(
          new FacultyDashboardResponse.TodayClass("09:00 - 09:50", "CSE-A", "AI Systems", "CSE-201"),
          new FacultyDashboardResponse.TodayClass("11:00 - 11:50", "AI&DS-C", "Networks", "CSE-305")
      );
    }

    List<FacultyDashboardResponse.TodayClass> output = new ArrayList<>();
    int index = 0;
    for (String className : classList) {
      output.add(new FacultyDashboardResponse.TodayClass(
          index == 0 ? "09:00 - 09:50" : "11:00 - 11:50",
          className,
          index == 0 ? "AI Systems" : "Networks",
          index == 0 ? "CSE-201" : "CSE-305"
      ));
      index++;
    }

    return output;
  }

  private String dayLabel(LocalDate date) {
    return date.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
  }

  private double calculateCgpa(List<StudentMarks> marks) {
    double avg = marks.stream()
        .mapToDouble(mark -> {
          double c1 = Optional.ofNullable(mark.getCat1()).orElse(0.0);
          double c2 = Optional.ofNullable(mark.getCat2()).orElse(0.0);
          double c3 = Optional.ofNullable(mark.getCat3()).orElse(0.0);
          return (c1 + c2 + c3) / 3.0;
        })
        .average()
        .orElse(0.0);
    return Math.round((avg / 10.0) * 100.0) / 100.0;
  }

  private String departmentRoom(String department, String room) {
    if (department == null || department.isBlank()) {
      return "Block-" + room;
    }
    return department.toUpperCase(Locale.ENGLISH) + "-" + room;
  }
}
