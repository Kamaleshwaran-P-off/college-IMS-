package com.smartcampus.platform.assignmentplanner.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.assignmentplanner.dto.ScheduledTaskResponse;
import com.smartcampus.platform.assignmentplanner.entity.PlannerAssignment;
import com.smartcampus.platform.assignmentplanner.entity.ScheduledTask;
import com.smartcampus.platform.assignmentplanner.repository.ScheduledTaskRepository;
import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.defaultdata.RealisticDataGenerator;
import com.smartcampus.platform.student.entity.Student;
import com.smartcampus.platform.student.service.StudentProfileService;

@Service
@Transactional
public class SmartSchedulerService {
  private static final int DEFAULT_MAX_HOURS = 3;
  private final AssignmentPlannerService plannerService;
  private final ScheduledTaskRepository taskRepository;
  private final StudentProfileService studentProfileService;
  private final RealisticDataGenerator dataGenerator;

  public SmartSchedulerService(
      AssignmentPlannerService plannerService,
      ScheduledTaskRepository taskRepository,
      StudentProfileService studentProfileService,
      RealisticDataGenerator dataGenerator
  ) {
    this.plannerService = plannerService;
    this.taskRepository = taskRepository;
    this.studentProfileService = studentProfileService;
    this.dataGenerator = dataGenerator;
  }

  public List<ScheduledTaskResponse> generateSchedule(User user, Integer maxHoursPerDay) {
    if (user.getRole() != Role.STUDENT) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
    }
    Student student = studentProfileService.ensureForUser(user);
    List<PlannerAssignment> assignments = plannerService.fetchAssignmentsForStudent(student);

    taskRepository.deleteByStudentId(student.getId());
    if (assignments.isEmpty()) {
      return dataGenerator.getDefaultPlannerTasks();
    }

    int cap = maxHoursPerDay != null && maxHoursPerDay > 0 ? maxHoursPerDay : DEFAULT_MAX_HOURS;
    LocalDate today = LocalDate.now();
    Map<LocalDate, Double> dayLoad = new HashMap<>();
    List<ScheduledTask> tasks = new ArrayList<>();

    assignments.sort(Comparator.comparing(PlannerAssignment::getDeadline, Comparator.nullsLast(Comparator.naturalOrder())));

    for (PlannerAssignment assignment : assignments) {
      double remaining = assignment.getEstimatedHours() == null || assignment.getEstimatedHours() <= 0
          ? 2.0
          : assignment.getEstimatedHours();
      LocalDate deadline = assignment.getDeadline() != null ? assignment.getDeadline() : today.plusDays(7);
      LocalDate cursor = today;

      while (remaining > 0) {
        if (cursor.isAfter(deadline)) {
          double hours = remaining;
          tasks.add(buildTask(assignment, student.getId(), deadline, hours));
          dayLoad.put(deadline, dayLoad.getOrDefault(deadline, 0.0) + hours);
          remaining = 0;
          break;
        }

        double used = dayLoad.getOrDefault(cursor, 0.0);
        double available = cap - used;
        if (available <= 0) {
          cursor = cursor.plusDays(1);
          continue;
        }
        double hours = Math.min(available, remaining);
        tasks.add(buildTask(assignment, student.getId(), cursor, hours));
        dayLoad.put(cursor, used + hours);
        remaining -= hours;
        if (available <= hours) {
          cursor = cursor.plusDays(1);
        }
      }
    }

    taskRepository.saveAll(tasks);
    return tasks.stream()
        .sorted(Comparator.comparing(ScheduledTask::getTaskDate))
        .map(this::toResponse)
        .toList();
  }

  public List<ScheduledTaskResponse> getScheduleForStudent(User user, Long studentId) {
    if (user.getRole() != Role.STUDENT) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
    }
    Student student = studentProfileService.ensureForUser(user);
    if (!student.getId().equals(studentId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }
    List<ScheduledTaskResponse> tasks = taskRepository.findByStudentIdOrderByTaskDateAsc(studentId).stream()
        .map(this::toResponse)
        .toList();
    return tasks.isEmpty() ? dataGenerator.getDefaultPlannerTasks() : tasks;
  }

  public ScheduledTaskResponse markCompleted(User user, Long taskId) {
    if (user.getRole() != Role.STUDENT) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
    }
    Student student = studentProfileService.ensureForUser(user);
    ScheduledTask task = taskRepository.findByIdAndStudentId(taskId, student.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found"));
    task.setCompleted(true);
    return toResponse(taskRepository.save(task));
  }

  private ScheduledTask buildTask(PlannerAssignment assignment, Long studentId, LocalDate date, double hours) {
    String detail = "Work on " + assignment.getTitle();
    return new ScheduledTask(
        assignment,
        studentId,
        date,
        detail,
        hours,
        false,
        LocalDateTime.now()
    );
  }

  private ScheduledTaskResponse toResponse(ScheduledTask task) {
    PlannerAssignment assignment = task.getAssignment();
    return new ScheduledTaskResponse(
        task.getId(),
        assignment != null ? assignment.getId() : null,
        assignment != null ? assignment.getTitle() : "Assignment",
        task.getTaskDate(),
        task.getTaskDetail(),
        task.getHours(),
        task.isCompleted(),
        assignment != null ? assignment.getDeadline() : null
    );
  }
}
