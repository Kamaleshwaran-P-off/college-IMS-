package com.smartcampus.platform.planner.service;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.chat.service.GeminiService;
import com.smartcampus.platform.common.exception.ResourceNotFoundException;
import com.smartcampus.platform.defaultdata.RealisticDataGenerator;
import com.smartcampus.platform.planner.dto.StudyPlanRequest;
import com.smartcampus.platform.planner.dto.StudyPlanHistoryResponse;
import com.smartcampus.platform.planner.dto.StudyPlanMultiRequest;
import com.smartcampus.platform.planner.dto.StudyPlanResponse;
import com.smartcampus.platform.planner.dto.StudyStreakResponse;
import com.smartcampus.platform.planner.dto.StudyTaskResponse;
import com.smartcampus.platform.planner.entity.RiskLevel;
import com.smartcampus.platform.planner.entity.StudyPlan;
import com.smartcampus.platform.planner.entity.StudyTask;
import com.smartcampus.platform.planner.repository.StudyPlanRepository;
import com.smartcampus.platform.planner.repository.StudyTaskRepository;

@Service
@Transactional
public class StudyPlannerService {
  private final StudyPlanRepository planRepository;
  private final StudyTaskRepository taskRepository;
  private final UserRepository userRepository;
  private final GeminiService geminiService;
  private final ObjectMapper objectMapper;
  private final RealisticDataGenerator dataGenerator;

  public StudyPlannerService(
      StudyPlanRepository planRepository,
      StudyTaskRepository taskRepository,
      UserRepository userRepository,
      GeminiService geminiService,
      ObjectMapper objectMapper,
      RealisticDataGenerator dataGenerator
  ) {
    this.planRepository = planRepository;
    this.taskRepository = taskRepository;
    this.userRepository = userRepository;
    this.geminiService = geminiService;
    this.objectMapper = objectMapper;
    this.dataGenerator = dataGenerator;
  }

  public StudyPlanResponse createPlan(StudyPlanRequest request) {
    return createPlanForWeek(request, resolveWeekStart(request.getWeekStart()));
  }

  public List<StudyPlanResponse> createMultiPlan(StudyPlanMultiRequest request) {
    int weeks = request.getWeeks();
    LocalDate baseWeekStart = resolveWeekStart(request.getWeekStart());
    List<StudyPlanResponse> plans = new ArrayList<>();
    for (int i = 0; i < weeks; i++) {
      StudyPlanResponse plan = createPlanForWeek(request, baseWeekStart.plusDays(i * 7L));
      plans.add(plan);
    }
    return plans;
  }

  private StudyPlanResponse createPlanForWeek(StudyPlanRequest request, LocalDate weekStart) {
    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    String marksJson = toJson(request.getMarks());
    String weakJson = toJson(request.getWeakSubjects());
    String assignmentsJson = toJson(request.getAssignments());

    String systemPrompt = "You are an academic study planner. Return strictly JSON.";
    String userPrompt = buildPrompt(request.getRiskLevel(), weekStart, marksJson, weakJson, assignmentsJson);

    ParsedPlan parsed;
    try {
      String reply = geminiService.generateStructuredResponse(systemPrompt, userPrompt);
      parsed = parsePlan(reply);
    } catch (Exception ex) {
      parsed = new ParsedPlan("Weekly study plan", new ArrayList<>());
    }

    StudyPlan plan = new StudyPlan(
        user,
        weekStart,
        request.getRiskLevel() != null ? request.getRiskLevel() : RiskLevel.MEDIUM,
        marksJson,
        weakJson,
        assignmentsJson,
        parsed.overview
    );
    planRepository.save(plan);

    List<StudyTask> tasks = new ArrayList<>();
    int order = 1;
    for (ParsedTask task : parsed.tasks) {
      tasks.add(new StudyTask(plan, order++, task.day, task.title, task.details));
    }

    if (tasks.isEmpty()) {
      tasks = fallbackTasks(plan);
    }

    taskRepository.saveAll(tasks);

    return toResponse(plan, tasks);
  }

  public StudyPlanResponse getLatest(Long userId) {
    StudyPlan plan = planRepository.findFirstByUserIdOrderByCreatedAtDesc(userId)
        .orElse(null);
    if (plan == null) {
      return dataGenerator.getDefaultStudyPlan();
    }
    List<StudyTask> tasks = taskRepository.findByPlanIdOrderByDayOrderAsc(plan.getId());
    return toResponse(plan, tasks);
  }

  public StudyPlanResponse getById(Long id) {
    StudyPlan plan = planRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Study plan not found"));
    List<StudyTask> tasks = taskRepository.findByPlanIdOrderByDayOrderAsc(plan.getId());
    return toResponse(plan, tasks);
  }

  public String exportCalendar(Long planId) {
    StudyPlan plan = planRepository.findById(planId)
        .orElseThrow(() -> new ResourceNotFoundException("Study plan not found"));
    List<StudyTask> tasks = taskRepository.findByPlanIdOrderByDayOrderAsc(plan.getId());
    return buildIcs(plan, tasks);
  }

  public StudyTaskResponse updateTaskCompletion(Long taskId, boolean completed) {
    StudyTask task = taskRepository.findById(taskId)
        .orElseThrow(() -> new ResourceNotFoundException("Study task not found"));
    task.setCompleted(completed);
    StudyTask saved = taskRepository.save(task);
    return toTaskResponse(saved);
  }

  public StudyTaskResponse updateTaskReminder(Long taskId, LocalDateTime reminderAt) {
    StudyTask task = taskRepository.findById(taskId)
        .orElseThrow(() -> new ResourceNotFoundException("Study task not found"));
    if (reminderAt == null) {
      task.setReminderAt(null);
    } else {
      task.setReminderAt(reminderAt);
    }
    StudyTask saved = taskRepository.save(task);
    return toTaskResponse(saved);
  }

  public List<StudyPlanHistoryResponse> getHistory(Long userId) {
    try {
      List<StudyPlan> plans = planRepository.findByUserIdOrderByCreatedAtDesc(userId);
      if (plans.isEmpty()) {
        return dataGenerator.getDefaultStudyPlanHistory();
      }
      return plans.stream().map(plan -> {
        long total = taskRepository.countByPlanId(plan.getId());
        long completed = taskRepository.countByPlanIdAndCompletedTrue(plan.getId());
        return new StudyPlanHistoryResponse(
            plan.getId(),
            plan.getWeekStart(),
            plan.getRiskLevel(),
            plan.getCreatedAt(),
            completed,
            total
        );
      }).toList();
    } catch (Exception ex) {
      return dataGenerator.getDefaultStudyPlanHistory();
    }
  }

  public StudyStreakResponse getStreak(Long userId) {
    try {
      List<StudyTask> completed = taskRepository.findCompletedByUser(userId);
      if (completed.isEmpty()) {
        return dataGenerator.getDefaultStudyStreak();
      }

      Set<LocalDate> uniqueDates = new LinkedHashSet<>();
      for (StudyTask task : completed) {
        uniqueDates.add(task.getCompletedAt().toLocalDate());
      }

      List<LocalDate> dates = new ArrayList<>(uniqueDates);
      int currentStreak = 0;
      int longestStreak = 0;
      LocalDate today = LocalDate.now();

      if (!dates.isEmpty()) {
        LocalDate cursor = today;
        if (!dates.contains(today) && dates.contains(today.minusDays(1))) {
          cursor = today.minusDays(1);
        }

        while (dates.contains(cursor)) {
          currentStreak++;
          cursor = cursor.minusDays(1);
        }

        int streak = 0;
        LocalDate previous = null;
        for (LocalDate date : dates) {
          if (previous == null || previous.minusDays(1).equals(date)) {
            streak++;
          } else {
            longestStreak = Math.max(longestStreak, streak);
            streak = 1;
          }
          previous = date;
        }
        longestStreak = Math.max(longestStreak, streak);
      }

      return new StudyStreakResponse(currentStreak, longestStreak, dates.get(0));
    } catch (Exception ex) {
      return dataGenerator.getDefaultStudyStreak();
    }
  }

  private String buildPrompt(RiskLevel riskLevel, LocalDate weekStart, String marksJson, String weakJson, String assignmentsJson) {
    return "Create a 7-day study plan starting " + weekStart + ".\n"
        + "Risk level: " + riskLevel + "\n"
        + "Student marks (JSON): " + marksJson + "\n"
        + "Weak subjects (JSON): " + weakJson + "\n"
        + "Assignments (JSON): " + assignmentsJson + "\n"
        + "Return JSON with keys: overview, tasks.\n"
        + "tasks: array of 7 items, each item has day, title, details.";
  }

  private String toJson(Object value) {
    try {
      return value == null ? "[]" : objectMapper.writeValueAsString(value);
    } catch (IOException ex) {
      return "[]";
    }
  }

  private ParsedPlan parsePlan(String reply) {
    try {
      JsonNode root = objectMapper.readTree(reply);
      String overview = root.path("overview").asText("Weekly study plan");
      List<ParsedTask> tasks = new ArrayList<>();
      JsonNode tasksNode = root.path("tasks");
      if (tasksNode.isArray()) {
        for (JsonNode node : tasksNode) {
          String day = node.path("day").asText("Day");
          String title = node.path("title").asText("Study session");
          String details = node.path("details").asText("");
          tasks.add(new ParsedTask(day, title, details));
        }
      }
      return new ParsedPlan(overview, tasks);
    } catch (IOException ex) {
      return new ParsedPlan(reply, new ArrayList<>());
    }
  }

  private List<StudyTask> fallbackTasks(StudyPlan plan) {
    List<StudyTask> tasks = new ArrayList<>();
    String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
    for (int i = 0; i < days.length; i++) {
      tasks.add(new StudyTask(plan, i + 1, days[i], "Focused study", "Review weak subjects and complete assignments."));
    }
    return tasks;
  }

  private StudyPlanResponse toResponse(StudyPlan plan, List<StudyTask> tasks) {
    List<StudyTaskResponse> taskResponses = tasks.stream().map(this::toTaskResponse).toList();
    return new StudyPlanResponse(
        plan.getId(),
        plan.getWeekStart(),
        plan.getRiskLevel(),
        plan.getPlanText(),
        taskResponses,
        plan.getCreatedAt()
    );
  }

  private StudyTaskResponse toTaskResponse(StudyTask task) {
    return new StudyTaskResponse(
        task.getId(),
        task.getDayOrder(),
        task.getDayLabel(),
        task.getTitle(),
        task.getDetails(),
        task.isCompleted(),
        task.getReminderAt() != null ? task.getReminderAt().toString() : null
    );
  }

  private LocalDate resolveWeekStart(LocalDate weekStart) {
    return weekStart != null ? weekStart : LocalDate.now().with(DayOfWeek.MONDAY);
  }

  private String buildIcs(StudyPlan plan, List<StudyTask> tasks) {
    StringBuilder sb = new StringBuilder();
    sb.append("BEGIN:VCALENDAR\n");
    sb.append("VERSION:2.0\n");
    sb.append("PRODID:-//SmartCampus//StudyPlan//EN\n");
    for (StudyTask task : tasks) {
      LocalDate date = plan.getWeekStart().plusDays(task.getDayOrder() - 1L);
      String dateStr = date.toString().replace("-", "");
      sb.append("BEGIN:VEVENT\n");
      sb.append("UID:plan-").append(plan.getId()).append("-task-").append(task.getId()).append("\n");
      sb.append("DTSTART;VALUE=DATE:").append(dateStr).append("\n");
      sb.append("DTEND;VALUE=DATE:").append(date.plusDays(1).toString().replace("-", "")).append("\n");
      sb.append("SUMMARY:").append(escapeIcs(task.getTitle())).append("\n");
      if (task.getDetails() != null && !task.getDetails().isBlank()) {
        sb.append("DESCRIPTION:").append(escapeIcs(task.getDetails())).append("\n");
      }
      sb.append("END:VEVENT\n");
    }
    sb.append("END:VCALENDAR\n");
    return sb.toString();
  }

  private String escapeIcs(String value) {
    return value.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,").replace("\n", "\\n");
  }

  private record ParsedPlan(String overview, List<ParsedTask> tasks) {}
  private record ParsedTask(String day, String title, String details) {}
}
