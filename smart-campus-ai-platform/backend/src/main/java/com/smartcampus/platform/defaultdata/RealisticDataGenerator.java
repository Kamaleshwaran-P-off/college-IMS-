package com.smartcampus.platform.defaultdata;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Service;

import com.smartcampus.platform.aiquiz.dto.AiQuizQuestion;
import com.smartcampus.platform.aiquiz.dto.AiQuizResponse;
import com.smartcampus.platform.assignmentplanner.dto.PlannerAssignmentResponse;
import com.smartcampus.platform.assignmentplanner.dto.ScheduledTaskResponse;
import com.smartcampus.platform.assignmentplanner.entity.AssignmentTargetType;
import com.smartcampus.platform.common.dto.PagedResponse;
import com.smartcampus.platform.coursework.assignment.dto.CourseAssignmentResponse;
import com.smartcampus.platform.coursework.material.dto.StudyMaterialResponse;
import com.smartcampus.platform.doubt.dto.DoubtResponse;
import com.smartcampus.platform.doubt.entity.DoubtStatus;
import com.smartcampus.platform.gmail.dto.GmailEmailResponse;
import com.smartcampus.platform.learning.dto.LearningCourseDetailResponse;
import com.smartcampus.platform.learning.dto.LearningCourseSummary;
import com.smartcampus.platform.learning.dto.LearningProgressResponse;
import com.smartcampus.platform.learning.dto.LearningTopicResponse;
import com.smartcampus.platform.planner.dto.StudyPlanHistoryResponse;
import com.smartcampus.platform.planner.dto.StudyPlanResponse;
import com.smartcampus.platform.planner.dto.StudyStreakResponse;
import com.smartcampus.platform.planner.dto.StudyTaskResponse;
import com.smartcampus.platform.planner.entity.RiskLevel;

@Service
public class RealisticDataGenerator {
  public static final List<String> SUBJECTS = List.of(
      "Operating Systems",
      "Data Analytics",
      "Design and Analysis of Algorithms",
      "Machine Learning"
  );

  private final List<String> facultyNames = List.of(
      "Dr. Kumar",
      "Prof. Meena",
      "Dr. Anitha",
      "Prof. Raj"
  );

  private LocalDate futureDate(int minDays, int maxDays) {
    int diff = Math.max(0, maxDays - minDays);
    return LocalDate.now().plusDays(minDays + ThreadLocalRandom.current().nextInt(diff + 1));
  }

  public AiQuizResponse getDefaultQuiz() {
    List<QuestionTemplate> pool = new ArrayList<>(buildQuestionPool());
    java.util.Collections.shuffle(pool, ThreadLocalRandom.current());
    int pick = Math.min(8, pool.size());
    List<AiQuizResponse.AiQuizQuestionView> questions = new ArrayList<>();
    for (int i = 0; i < pick; i++) {
      QuestionTemplate template = pool.get(i);
      questions.add(new AiQuizResponse.AiQuizQuestionView(i, template.type, template.question, template.options));
    }

    return new AiQuizResponse(
        -101L,
        "Practice Quiz: Core Concepts",
        "CSE",
        "CSE-A",
        true,
        20,
        questions
    );
  }

  public List<AiQuizQuestion> getDefaultQuizQuestions() {
    List<QuestionTemplate> pool = new ArrayList<>(buildQuestionPool());
    java.util.Collections.shuffle(pool, ThreadLocalRandom.current());
    int pick = Math.min(8, pool.size());
    List<AiQuizQuestion> questions = new ArrayList<>();
    for (int i = 0; i < pick; i++) {
      QuestionTemplate template = pool.get(i);
      questions.add(new AiQuizQuestion(template.type, template.question, template.options, template.answer));
    }
    return questions;
  }

  public List<CourseAssignmentResponse> getDefaultAssignments() {
    LocalDateTime now = LocalDateTime.now();
    List<AssignmentTemplate> templates = new ArrayList<>(List.of(
        new AssignmentTemplate("CPU Scheduling Analysis", "Compare FCFS, SJF, and Round Robin for the given process set.", "Operating Systems"),
        new AssignmentTemplate("Deadlock Prevention Techniques", "Summarize prevention strategies with examples.", "Operating Systems"),
        new AssignmentTemplate("Regression Model Implementation", "Build a linear regression model and evaluate with MSE.", "Machine Learning"),
        new AssignmentTemplate("Data Visualization Dashboard", "Create charts for dataset insights using best practices.", "Data Analytics"),
        new AssignmentTemplate("Knapsack Optimization Study", "Implement 0/1 knapsack using dynamic programming.", "Design and Analysis of Algorithms"),
        new AssignmentTemplate("Greedy vs DP Case Study", "Analyze when greedy fails and DP succeeds.", "Design and Analysis of Algorithms")
    ));
    java.util.Collections.shuffle(templates, ThreadLocalRandom.current());
    long idSeed = -201L;
    List<CourseAssignmentResponse> responses = new ArrayList<>();
    for (int i = 0; i < Math.min(3, templates.size()); i++) {
      AssignmentTemplate template = templates.get(i);
      String faculty = facultyNames.get(ThreadLocalRandom.current().nextInt(facultyNames.size()));
      responses.add(new CourseAssignmentResponse(
          idSeed--,
          template.title,
          template.description,
          futureDate(2, 7),
          "CSE",
          "CSE-A",
          faculty,
          now.minusDays(1 + i),
          now.minusDays(1 + i),
          false,
          true
      ));
    }
    return responses;
  }

  public List<StudyMaterialResponse> getDefaultStudyMaterials() {
    LocalDateTime now = LocalDateTime.now();
    List<StudyMaterialResponse> materials = new ArrayList<>();
    List<String> titles = List.of(
        "OS - Process Management Notes",
        "ML - Regression Basics",
        "DAA - Dynamic Programming Guide",
        "Data Analytics - Visualization Toolkit",
        "OS - Memory Management Cheatsheet",
        "ML - Model Evaluation Metrics"
    );
    java.util.Collections.shuffle(titles, ThreadLocalRandom.current());
    long idSeed = -301L;
    for (int i = 0; i < Math.min(4, titles.size()); i++) {
      materials.add(new StudyMaterialResponse(idSeed--, titles.get(i),
          "Quick reference notes and curated examples for this module.",
          "CSE", "CSE-A",
          facultyNames.get(i % facultyNames.size()),
          now.minusDays(2 + i),
          now.minusDays(2 + i),
          false,
          true));
    }
    return materials;
  }

  public List<GmailEmailResponse> getDefaultInbox() {
    return List.of(
        new GmailEmailResponse("msg-101", "Assignment deadline updated: CPU Scheduling Analysis", "dr.kumar@campus.edu",
            "The submission window has been extended by two days. Please check the assignments page.", LocalDate.now().minusDays(1).toString(), "DEADLINES", true),
        new GmailEmailResponse("msg-102", "New study material uploaded: Regression Basics", "prof.meena@campus.edu",
            "Regression notes and examples are now available in Study Materials.", LocalDate.now().minusDays(2).toString(), "OFFICIAL", true),
        new GmailEmailResponse("msg-103", "Quiz results published: Core Concepts", "quiz@campus.edu",
            "Your quiz performance report is ready. Visit the quiz section to view feedback.", LocalDate.now().minusDays(3).toString(), "OFFICIAL", false)
    );
  }

  public List<LearningCourseSummary> getDefaultLearningCourses() {
    return List.of(
        new LearningCourseSummary(-401L, "Machine Learning", "AI-structured learning roadmap", LocalDateTime.now().minusDays(1), 3),
        new LearningCourseSummary(-402L, "Operating Systems", "Core OS concepts and process management", LocalDateTime.now().minusDays(2), 3)
    );
  }

  public LearningCourseDetailResponse getDefaultLearningCourseDetail(Long courseId) {
    List<LearningTopicResponse> topics = List.of(
        new LearningTopicResponse(-410L, "Foundations", "Key ML terminology and evaluation metrics", 1, "UNLOCKED", null),
        new LearningTopicResponse(-411L, "Supervised Learning", "Regression and classification approaches", 2, "LOCKED", null),
        new LearningTopicResponse(-412L, "Neural Networks", "Perceptrons, MLPs, and backpropagation", 3, "LOCKED", null)
    );
    return new LearningCourseDetailResponse(courseId != null ? courseId : -401L, "Machine Learning", "AI-structured learning roadmap", topics);
  }

  public LearningProgressResponse getDefaultLearningProgress() {
    return new LearningProgressResponse(1, 3, 33.3, 140, 4, "Rising Star", List.of(3, 2, 3));
  }

  public List<PlannerAssignmentResponse> getDefaultPlannerAssignments() {
    LocalDate today = LocalDate.now();
    List<PlannerAssignmentResponse> responses = new ArrayList<>();
    List<AssignmentTemplate> templates = new ArrayList<>(List.of(
        new AssignmentTemplate("CPU Scheduling Analysis", "Solve FCFS, SJF, and RR cases.", "Operating Systems"),
        new AssignmentTemplate("Regression Model Implementation", "Build and evaluate a regression model.", "Machine Learning"),
        new AssignmentTemplate("Knapsack Optimization Study", "Implement DP for knapsack.", "Design and Analysis of Algorithms")
    ));
    java.util.Collections.shuffle(templates, ThreadLocalRandom.current());
    long idSeed = -501L;
    for (int i = 0; i < Math.min(2, templates.size()); i++) {
      AssignmentTemplate template = templates.get(i);
      responses.add(new PlannerAssignmentResponse(
          idSeed--,
          template.title,
          template.description,
          today.plusDays(3 + i),
          2.0 + (i * 0.5),
          AssignmentTargetType.CLASS,
          null,
          "CSE",
          "A",
          facultyNames.get(i % facultyNames.size()),
          LocalDateTime.now().minusDays(1 + i)
      ));
    }
    return responses;
  }

  public List<ScheduledTaskResponse> getDefaultPlannerTasks() {
    LocalDate today = LocalDate.now();
    List<PlannerAssignmentResponse> assignments = getDefaultPlannerAssignments();
    List<ScheduledTaskResponse> tasks = new ArrayList<>();
    long idSeed = -601L;
    int dayOffset = 1;
    for (PlannerAssignmentResponse assignment : assignments) {
      tasks.add(new ScheduledTaskResponse(
          idSeed--,
          assignment.id(),
          assignment.title(),
          today.plusDays(dayOffset++),
          "Work on " + assignment.title() + " (" + assignment.estimatedHours() + "h)",
          assignment.estimatedHours(),
          false,
          assignment.deadline()
      ));
    }
    return tasks;
  }

  public PagedResponse<DoubtResponse> getDefaultDoubtsPage(int page, int size) {
    List<DoubtResponse> content = List.of(
        new DoubtResponse(-701L, -1L, -1L, "Deadlock scenarios in OS",
            "How do circular waits typically arise in real systems?", DoubtStatus.OPEN, LocalDateTime.now().minusDays(2), null),
        new DoubtResponse(-702L, -1L, -1L, "Greedy choice property",
            "Why does activity selection work with greedy?", DoubtStatus.ANSWERED, LocalDateTime.now().minusDays(3), null)
    );
    return new PagedResponse<>(content, page, size, content.size(), 1);
  }

  public StudyPlanResponse getDefaultStudyPlan() {
    LocalDate weekStart = LocalDate.now().with(DayOfWeek.MONDAY);
    List<StudyTaskResponse> tasks = List.of(
        new StudyTaskResponse(-801L, 1, "Monday", "Data Analytics Review (2h)", "Revise visualization techniques", false, null),
        new StudyTaskResponse(-802L, 2, "Tuesday", "DAA Practice (2h)", "Solve knapsack and shortest path", false, null),
        new StudyTaskResponse(-803L, 3, "Wednesday", "ML Regression Exercises (1.5h)", "Work through regression problems", false, null)
    );
    return new StudyPlanResponse(-800L, weekStart, RiskLevel.MEDIUM, "Balanced weekly plan focusing on core subjects.", tasks, LocalDateTime.now());
  }

  public List<StudyPlanHistoryResponse> getDefaultStudyPlanHistory() {
    return List.of(
        new StudyPlanHistoryResponse(-810L, LocalDate.now().with(DayOfWeek.MONDAY), RiskLevel.MEDIUM, LocalDateTime.now().minusDays(1), 1, 3)
    );
  }

  public StudyStreakResponse getDefaultStudyStreak() {
    return new StudyStreakResponse(2, 5, LocalDate.now().minusDays(1));
  }

  public CertificateSample getDefaultCertificate() {
    return new CertificateSample("Kamaleshwaran P", "Machine Learning", 8, 10, 120);
  }

  public record CertificateSample(String studentName, String subject, int score, int totalMarks, int timeTaken) {}

  private List<QuestionTemplate> buildQuestionPool() {
    return List.of(
        new QuestionTemplate("MCQ", "Operating Systems: Which scheduling algorithm gives each process a time slice?",
            List.of("Round Robin", "FCFS", "SJF", "Priority (non-preemptive)"), "Round Robin"),
        new QuestionTemplate("MCQ", "Operating Systems: Deadlock avoidance uses which algorithm?",
            List.of("Banker's Algorithm", "Dijkstra", "Kruskal", "Bellman-Ford"), "Banker's Algorithm"),
        new QuestionTemplate("MCQ", "Operating Systems: Which system call creates a new process?",
            List.of("fork()", "exec()", "wait()", "kill()"), "fork()"),
        new QuestionTemplate("MCQ", "Data Analytics: Which metric is best for measuring model error in regression?",
            List.of("Mean Squared Error", "Accuracy", "F1 Score", "Precision"), "Mean Squared Error"),
        new QuestionTemplate("MCQ", "Data Analytics: Which chart is ideal for showing distribution?",
            List.of("Histogram", "Pie chart", "Line chart", "Radar chart"), "Histogram"),
        new QuestionTemplate("MCQ", "Data Analytics: Outliers are commonly detected using?",
            List.of("Box Plot", "Gantt Chart", "Heat Map", "Tree Map"), "Box Plot"),
        new QuestionTemplate("MCQ", "DAA: Which algorithm uses divide and conquer strategy?",
            List.of("Merge Sort", "Insertion Sort", "Selection Sort", "Bubble Sort"), "Merge Sort"),
        new QuestionTemplate("MCQ", "DAA: The time complexity of binary search is:",
            List.of("O(log n)", "O(n)", "O(n log n)", "O(1)"), "O(log n)"),
        new QuestionTemplate("MCQ", "DAA: Which technique solves optimal substructure problems?",
            List.of("Dynamic Programming", "Greedy", "Backtracking", "Brute Force"), "Dynamic Programming"),
        new QuestionTemplate("MCQ", "Machine Learning: Overfitting means the model:",
            List.of("Fits training data too well", "Cannot learn", "Is too simple", "Has no bias"), "Fits training data too well"),
        new QuestionTemplate("MCQ", "Machine Learning: Which is a supervised algorithm?",
            List.of("Logistic Regression", "K-Means", "PCA", "Apriori"), "Logistic Regression"),
        new QuestionTemplate("MCQ", "Machine Learning: Which activation function outputs values between 0 and 1?",
            List.of("Sigmoid", "ReLU", "Tanh", "Softplus"), "Sigmoid")
    );
  }

  private record QuestionTemplate(String type, String question, List<String> options, String answer) {}
  private record AssignmentTemplate(String title, String description, String subject) {}
}
