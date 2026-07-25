package com.smartcampus.platform.defaultdata;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

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
public class DefaultDataProvider {
  public static final List<String> SUBJECTS = List.of(
      "Operating Systems",
      "Data Analytics",
      "Design and Analysis of Algorithms",
      "Machine Learning"
  );

  public AiQuizResponse getDefaultQuiz() {
    List<AiQuizResponse.AiQuizQuestionView> questions = List.of(
        new AiQuizResponse.AiQuizQuestionView(0, "MCQ", "Operating Systems: What is a process?", List.of("Program in execution", "File", "Thread", "Memory")),
        new AiQuizResponse.AiQuizQuestionView(1, "MCQ", "Operating Systems: Which scheduling is preemptive?", List.of("FCFS", "SJF", "Round Robin", "Priority (non-preemptive)")),
        new AiQuizResponse.AiQuizQuestionView(2, "MCQ", "Operating Systems: Deadlock requires which condition?", List.of("Mutual exclusion", "Round robin", "Paging", "Spooling")),
        new AiQuizResponse.AiQuizQuestionView(3, "MCQ", "Operating Systems: Page fault occurs when?", List.of("Page not in memory", "Cache hit", "CPU idle", "DMA completes")),
        new AiQuizResponse.AiQuizQuestionView(4, "MCQ", "Operating Systems: CPU scheduler runs when?", List.of("Process blocks", "Process enters running", "Process terminates", "All of these")),

        new AiQuizResponse.AiQuizQuestionView(5, "MCQ", "Data Analytics: What is ETL?", List.of("Extract Transform Load", "Encode Transfer Link", "Evaluate Test Learn", "Event Trace Log")),
        new AiQuizResponse.AiQuizQuestionView(6, "MCQ", "Data Analytics: Which chart best shows distribution?", List.of("Histogram", "Line chart", "Pie chart", "Radar chart")),
        new AiQuizResponse.AiQuizQuestionView(7, "MCQ", "Data Analytics: Outliers can be detected using?", List.of("Box plot", "Scatter plot", "Bar chart", "Area chart")),
        new AiQuizResponse.AiQuizQuestionView(8, "MCQ", "Data Analytics: What is descriptive analytics?", List.of("Summarizing past data", "Predicting future", "Prescribing actions", "Automating ETL")),
        new AiQuizResponse.AiQuizQuestionView(9, "MCQ", "Data Analytics: Which is a dimensionality reduction technique?", List.of("PCA", "KNN", "Naive Bayes", "Apriori")),

        new AiQuizResponse.AiQuizQuestionView(10, "MCQ", "DAA: Time complexity of binary search?", List.of("O(log n)", "O(n)", "O(n log n)", "O(1)")),
        new AiQuizResponse.AiQuizQuestionView(11, "MCQ", "DAA: Which algorithm uses divide and conquer?", List.of("Merge Sort", "Selection Sort", "Insertion Sort", "Bubble Sort")),
        new AiQuizResponse.AiQuizQuestionView(12, "MCQ", "DAA: Greedy approach guarantees optimal for?", List.of("Activity selection", "Knapsack (0/1)", "All graphs", "All DP problems")),
        new AiQuizResponse.AiQuizQuestionView(13, "MCQ", "DAA: Stable sort?", List.of("Merge Sort", "Heap Sort", "Quick Sort", "Selection Sort")),
        new AiQuizResponse.AiQuizQuestionView(14, "MCQ", "DAA: Bellman-Ford handles?", List.of("Negative edges", "Only positive weights", "Only DAGs", "Only MST")),

        new AiQuizResponse.AiQuizQuestionView(15, "MCQ", "Machine Learning: Supervised learning uses?", List.of("Labeled data", "Unlabeled data", "No data", "Random labels")),
        new AiQuizResponse.AiQuizQuestionView(16, "MCQ", "Machine Learning: Which is a classification algorithm?", List.of("Logistic Regression", "K-Means", "PCA", "Apriori")),
        new AiQuizResponse.AiQuizQuestionView(17, "MCQ", "Machine Learning: Overfitting occurs when?", List.of("Model memorizes training data", "Model is too simple", "No training data", "Loss is constant")),
        new AiQuizResponse.AiQuizQuestionView(18, "MCQ", "Machine Learning: Which metric for regression?", List.of("MSE", "Accuracy", "Precision", "Recall")),
        new AiQuizResponse.AiQuizQuestionView(19, "MCQ", "Machine Learning: Gradient descent optimizes?", List.of("Loss function", "Data size", "CPU clock", "Network latency"))
    );

    return new AiQuizResponse(
        -101L,
        "Sample Quiz Bank",
        "CSE",
        "CSE-A",
        true,
        20,
        questions
    );
  }

  public List<CourseAssignmentResponse> getDefaultAssignments() {
    LocalDate today = LocalDate.now();
    LocalDateTime now = LocalDateTime.now();
    return List.of(
        new CourseAssignmentResponse(
            -201L,
            "CPU Scheduling Assignment",
            "Solve FCFS, SJF, and Round Robin for given processes.",
            today.plusDays(4),
            "CSE",
            "CSE-A",
            "Sample Faculty",
            now.minusDays(1),
            now.minusDays(1),
            false,
            true
        ),
        new CourseAssignmentResponse(
            -202L,
            "Knapsack Problem",
            "Implement 0/1 knapsack using dynamic programming.",
            today.plusDays(6),
            "CSE",
            "CSE-A",
            "Sample Faculty",
            now.minusDays(2),
            now.minusDays(2),
            false,
            true
        )
    );
  }

  public List<StudyMaterialResponse> getDefaultStudyMaterials() {
    LocalDateTime now = LocalDateTime.now();
    return List.of(
        new StudyMaterialResponse(-301L, "Operating Systems - Process Management Notes",
            "Quick notes on processes, threads, and scheduling.", "CSE", "CSE-A", "Sample Faculty", now.minusDays(2), now.minusDays(2), false, true),
        new StudyMaterialResponse(-302L, "Machine Learning - Regression Basics",
            "Linear regression, cost function, and gradient descent.", "CSE", "CSE-A", "Sample Faculty", now.minusDays(3), now.minusDays(3), false, true),
        new StudyMaterialResponse(-303L, "DAA - Dynamic Programming",
            "Optimal substructure and overlapping subproblems.", "CSE", "CSE-A", "Sample Faculty", now.minusDays(4), now.minusDays(4), false, true),
        new StudyMaterialResponse(-304L, "Data Analytics - Data Visualization",
            "Common charts and dashboards for analytics.", "CSE", "CSE-A", "Sample Faculty", now.minusDays(5), now.minusDays(5), false, true)
    );
  }

  public List<GmailEmailResponse> getDefaultInbox() {
    return List.of(
        new GmailEmailResponse("sample-1", "New assignment uploaded for Operating Systems", "faculty@campus.edu",
            "Check the assignments tab for CPU scheduling tasks.", LocalDate.now().minusDays(1).toString(), "OFFICIAL", true),
        new GmailEmailResponse("sample-2", "Machine Learning quiz available", "quiz@campus.edu",
            "Attempt the latest quiz to unlock your certificate.", LocalDate.now().minusDays(2).toString(), "DEADLINES", true),
        new GmailEmailResponse("sample-3", "Reminder: Submit DAA assignment", "faculty@campus.edu",
            "Please submit the knapsack problem by Friday.", LocalDate.now().minusDays(3).toString(), "DEADLINES", true)
    );
  }

  public List<LearningCourseSummary> getDefaultLearningCourses() {
    return List.of(
        new LearningCourseSummary(-401L, "Machine Learning", "Structured AI learning flow", LocalDateTime.now().minusDays(1), 3)
    );
  }

  public LearningCourseDetailResponse getDefaultLearningCourseDetail(Long courseId) {
    List<LearningTopicResponse> topics = List.of(
        new LearningTopicResponse(-410L, "Basics", "Intro to ML concepts and terminology", 1, "UNLOCKED", null),
        new LearningTopicResponse(-411L, "Supervised Learning", "Regression and classification foundations", 2, "LOCKED", null),
        new LearningTopicResponse(-412L, "Neural Networks", "Perceptrons, MLPs, and backprop", 3, "LOCKED", null)
    );
    return new LearningCourseDetailResponse(courseId != null ? courseId : -401L, "Machine Learning", "Structured AI learning flow", topics);
  }

  public LearningProgressResponse getDefaultLearningProgress() {
    return new LearningProgressResponse(1, 3, 33.3, 120, 3, "Rising Star", List.of(2, 3, 2));
  }

  public List<PlannerAssignmentResponse> getDefaultPlannerAssignments() {
    LocalDate today = LocalDate.now();
    return List.of(
        new PlannerAssignmentResponse(-501L, "CPU Scheduling Assignment", "Complete OS scheduling worksheet.",
            today.plusDays(3), 2.0, AssignmentTargetType.CLASS, null, "CSE", "A",
            "Sample Faculty", LocalDateTime.now().minusDays(1)),
        new PlannerAssignmentResponse(-502L, "ML Basics Reading", "Read regression notes and summarize key points.",
            today.plusDays(4), 1.5, AssignmentTargetType.CLASS, null, "CSE", "A",
            "Sample Faculty", LocalDateTime.now().minusDays(2))
    );
  }

  public List<ScheduledTaskResponse> getDefaultPlannerTasks() {
    LocalDate today = LocalDate.now();
    return List.of(
        new ScheduledTaskResponse(-601L, -501L, "CPU Scheduling Assignment", today.plusDays(1),
            "Complete Operating Systems Assignment (2h)", 2.0, false, today.plusDays(3)),
        new ScheduledTaskResponse(-602L, -502L, "ML Basics Reading", today.plusDays(2),
            "Study Machine Learning Basics (1.5h)", 1.5, false, today.plusDays(4))
    );
  }

  public PagedResponse<DoubtResponse> getDefaultDoubtsPage(int page, int size) {
    List<DoubtResponse> content = List.of(
        new DoubtResponse(-701L, -1L, -1L, "What is deadlock?", "Deadlock is a situation where processes wait indefinitely.", DoubtStatus.OPEN, LocalDateTime.now().minusDays(2), null),
        new DoubtResponse(-702L, -1L, -1L, "Explain greedy algorithm", "Greedy approach makes locally optimal choices at each step.", DoubtStatus.ANSWERED, LocalDateTime.now().minusDays(3), null)
    );
    return new PagedResponse<>(content, page, size, content.size(), 1);
  }

  public StudyPlanResponse getDefaultStudyPlan() {
    LocalDate weekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);
    List<StudyTaskResponse> tasks = List.of(
        new StudyTaskResponse(-801L, 1, "Monday", "Study Data Analytics (2h)", "Review visualizations and dashboards", false, null),
        new StudyTaskResponse(-802L, 2, "Tuesday", "Practice DAA problems (2h)", "Solve knapsack and shortest path", false, null)
    );
    return new StudyPlanResponse(-800L, weekStart, RiskLevel.MEDIUM, "Sample plan to keep you on track.", tasks, LocalDateTime.now());
  }

  public List<StudyPlanHistoryResponse> getDefaultStudyPlanHistory() {
    return List.of(
        new StudyPlanHistoryResponse(-810L, LocalDate.now().with(java.time.DayOfWeek.MONDAY), RiskLevel.MEDIUM, LocalDateTime.now().minusDays(1), 1, 2)
    );
  }

  public StudyStreakResponse getDefaultStudyStreak() {
    return new StudyStreakResponse(1, 2, LocalDate.now());
  }

  public CertificateSample getDefaultCertificate() {
    return new CertificateSample("Demo Student", "Machine Learning", 8, 10, 120);
  }

  public record CertificateSample(String studentName, String subject, int score, int totalMarks, int timeTaken) {}
}
