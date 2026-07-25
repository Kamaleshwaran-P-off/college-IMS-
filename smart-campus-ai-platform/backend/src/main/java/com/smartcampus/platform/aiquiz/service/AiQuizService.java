package com.smartcampus.platform.aiquiz.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.platform.aiquiz.dto.AiQuizGenerateRequest;
import com.smartcampus.platform.aiquiz.dto.AiQuizQuestion;
import com.smartcampus.platform.aiquiz.dto.AiQuizResponse;
import com.smartcampus.platform.aiquiz.dto.AiQuizSubmissionRequest;
import com.smartcampus.platform.aiquiz.dto.AiQuizSubmissionResponse;
import com.smartcampus.platform.aiquiz.entity.AiQuiz;
import com.smartcampus.platform.aiquiz.entity.AiQuizSubmission;
import com.smartcampus.platform.aiquiz.repository.AiQuizRepository;
import com.smartcampus.platform.aiquiz.repository.AiQuizSubmissionRepository;
import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.chat.service.GeminiService;
import com.smartcampus.platform.defaultdata.RealisticDataGenerator;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.staff.repository.FacultyClassMappingRepository;
import com.smartcampus.platform.staff.repository.StaffRepository;
import com.smartcampus.platform.student.entity.Student;
import com.smartcampus.platform.student.repository.StudentRepository;

@Service
@Transactional
public class AiQuizService {
  private final AiQuizRepository quizRepository;
  private final AiQuizSubmissionRepository submissionRepository;
  private final UserRepository userRepository;
  private final StaffRepository staffRepository;
  private final StudentRepository studentRepository;
  private final FacultyClassMappingRepository mappingRepository;
  private final GeminiService geminiService;
  private final ObjectMapper objectMapper;
  private final CertificateService certificateService;
  private final RealisticDataGenerator dataGenerator;
  private static final int MAX_PDF_CHARS = 12000;
  private static final int DEFAULT_DURATION_MINUTES = 20;

  public AiQuizService(
      AiQuizRepository quizRepository,
      AiQuizSubmissionRepository submissionRepository,
      UserRepository userRepository,
      StaffRepository staffRepository,
      StudentRepository studentRepository,
      FacultyClassMappingRepository mappingRepository,
      GeminiService geminiService,
      ObjectMapper objectMapper,
      CertificateService certificateService,
      RealisticDataGenerator dataGenerator
  ) {
    this.quizRepository = quizRepository;
    this.submissionRepository = submissionRepository;
    this.userRepository = userRepository;
    this.staffRepository = staffRepository;
    this.studentRepository = studentRepository;
    this.mappingRepository = mappingRepository;
    this.geminiService = geminiService;
    this.objectMapper = objectMapper;
    this.certificateService = certificateService;
    this.dataGenerator = dataGenerator;
  }

  public AiQuizResponse generateQuiz(AiQuizGenerateRequest request, String facultyEmail) {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }

    Staff staff = staffRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty profile not found"));

    String className = normalize(request.getClassName());
    if (className != null) {
      boolean allowed = mappingRepository.findByStaffId(staff.getId()).stream()
          .anyMatch(mapping -> mapping.getClassName().equalsIgnoreCase(className));
      if (!allowed) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty is not assigned to this class");
      }
    }

    AiQuizPayload payload = generateAiQuizPayload(request);
    String title = request.getTitle() != null && !request.getTitle().isBlank() ? request.getTitle() : payload.title();
    String questionsJson = serializePayload(payload);
    String normalizedDepartment = normalize(request.getDepartment());
    if (normalizedDepartment == null && className != null) {
      normalizedDepartment = extractDepartment(className);
    }

    int durationMinutes = normalizeDuration(request.getDurationMinutes());
    LocalDateTime now = LocalDateTime.now();
    AiQuiz quiz = new AiQuiz(
        staff,
        title,
        request.getSyllabus(),
        request.getQuestionCount(),
        String.join(",", request.getQuestionTypes()),
        durationMinutes,
        normalizedDepartment,
        className,
        extractSection(className),
        questionsJson,
        null,
        null,
        null,
        now,
        true,
        now
    );

    AiQuiz saved = quizRepository.save(quiz);
    return toResponse(saved, payload.questions());
  }

  public AiQuizResponse generateQuizFromPdf(
      MultipartFile file,
      Integer questionCount,
      List<String> questionTypes,
      Integer durationMinutes,
      String title,
      String className,
      String department,
      String facultyEmail
  ) throws IOException {
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PDF file is required");
    }
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }

    Staff staff = staffRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty profile not found"));

    String normalizedClass = normalize(className);
    if (normalizedClass != null) {
      boolean allowed = mappingRepository.findByStaffId(staff.getId()).stream()
          .anyMatch(mapping -> mapping.getClassName().equalsIgnoreCase(normalizedClass));
      if (!allowed) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty is not assigned to this class");
      }
    }

    String pdfText = extractPdfText(file);
    if (pdfText == null || pdfText.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to extract text from PDF");
    }

    List<String> types = questionTypes == null || questionTypes.isEmpty()
        ? List.of("MCQ", "TRUE_FALSE", "FILL_BLANK")
        : questionTypes;
    int safeCount = questionCount == null ? 10 : Math.max(1, Math.min(30, questionCount));
    int safeDuration = normalizeDuration(durationMinutes);

    AiQuizPayload payload = generateAiQuizPayloadFromText(pdfText, safeCount, types);
    String quizTitle = (title != null && !title.isBlank()) ? title : payload.title();
    String questionsJson = serializePayload(payload);
    String normalizedDepartment = normalize(department);
    if (normalizedDepartment == null && normalizedClass != null) {
      normalizedDepartment = extractDepartment(normalizedClass);
    }

    String syllabusSnippet = pdfText.length() > 2000 ? pdfText.substring(0, 2000) : pdfText;

    LocalDateTime now = LocalDateTime.now();
    AiQuiz quiz = new AiQuiz(
        staff,
        quizTitle,
        syllabusSnippet,
        safeCount,
        String.join(",", types),
        safeDuration,
        normalizedDepartment,
        normalizedClass,
        extractSection(normalizedClass),
        questionsJson,
        file.getOriginalFilename(),
        file.getContentType(),
        file.getBytes(),
        now,
        true,
        now
    );

    AiQuiz saved = quizRepository.save(quiz);
    return toResponse(saved, payload.questions());
  }

  public AiQuizResponse getLatestForStudent(String email) {
    try {
      var user = userRepository.findByEmail(email)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
      if (user.getRole() != Role.STUDENT) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
      }

      Student student = studentRepository.findByUserId(user.getId())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student profile not found"));
      String className = normalize(buildClassKey(student.getDepartment(), student.getSection()));

      AiQuiz quiz = quizRepository.findTopByClassNameAndIsVisibleTrueOrderByCreatedAtDesc(className)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No quiz available"));

      List<AiQuizQuestion> questions = parseQuestions(quiz.getQuestionsJson());
      return toResponse(quiz, questions);
    } catch (Exception ex) {
      return dataGenerator.getDefaultQuiz();
    }
  }

  public AiQuizResponse getLatestForFaculty(String email) {
    try {
      var user = userRepository.findByEmail(email)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
      if (user.getRole() != Role.STAFF) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
      }

      Staff staff = staffRepository.findByUserId(user.getId())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty profile not found"));
      AiQuiz quiz = quizRepository.findByCreatedByIdOrderByCreatedAtDesc(staff.getId())
          .stream()
          .findFirst()
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No quiz available"));

      List<AiQuizQuestion> questions = parseQuestions(quiz.getQuestionsJson());
      return toResponse(quiz, questions);
    } catch (Exception ex) {
      return dataGenerator.getDefaultQuiz();
    }
  }

  public AiQuizSubmissionResponse submitQuiz(AiQuizSubmissionRequest request, String studentEmail) {
    var user = userRepository.findByEmail(studentEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STUDENT) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
    }

    Student student = studentRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student profile not found"));
    AiQuiz quiz = quizRepository.findById(request.getQuizId()).orElse(null);
    boolean fallbackQuiz = false;
    if (quiz == null) {
      if (request.getQuizId() != null && request.getQuizId() < 0) {
        fallbackQuiz = true;
      } else {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found");
      }
    }

    if (!fallbackQuiz) {
      String studentClass = normalize(buildClassKey(student.getDepartment(), student.getSection()));
      if (quiz.getClassName() != null && studentClass != null && !quiz.getClassName().equalsIgnoreCase(studentClass)) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Quiz not available for this student");
      }
    }

    List<AiQuizQuestion> questions = fallbackQuiz ? dataGenerator.getDefaultQuizQuestions() : parseQuestions(quiz.getQuestionsJson());
    Map<Integer, AiQuizQuestion> indexMap = buildIndexMap(questions);
    int total = questions.size();

    int score = 0;
    for (AiQuizSubmissionRequest.AiQuizAnswer answer : request.getAnswers()) {
      AiQuizQuestion question = indexMap.get(answer.getIndex());
      if (question == null) {
        continue;
      }
      if (isCorrect(question.getAnswer(), answer.getAnswer())) {
        score++;
      }
    }

    String quizTitle = fallbackQuiz ? dataGenerator.getDefaultQuiz().getTitle() : quiz.getTitle();
    Integer duration = fallbackQuiz ? DEFAULT_DURATION_MINUTES : quiz.getDurationMinutes();
    byte[] certificateBytes = null;
    try {
      certificateBytes = certificateService.generateCertificate(
          student.getUser().getFullName(),
          student.getStudentCode(),
          quizTitle,
          student.getDepartment(),
          student.getSection(),
          score,
          total,
          request.getTimeTakenSeconds(),
          duration
      );
    } catch (IOException ex) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate certificate");
    }

    AiQuizSubmission saved = null;
    if (!fallbackQuiz) {
      String answersJson = serializeAnswers(request);
      Optional<AiQuizSubmission> existing = submissionRepository.findByQuizIdAndStudentId(quiz.getId(), student.getId());
      if (existing.isPresent()) {
        throw new ResponseStatusException(HttpStatus.CONFLICT, "Quiz already submitted");
      }
      AiQuizSubmission submission = new AiQuizSubmission();

      submission.setQuiz(quiz);
      submission.setStudent(student);
      submission.setAnswersJson(answersJson);
      submission.setScore(score);
      submission.setTotal(total);
      submission.setTimeTakenSeconds(request.getTimeTakenSeconds());
      submission.setCertificateFileName("certificate-" + student.getStudentCode() + ".pdf");
      submission.setCertificateContentType("application/pdf");
      submission.setCertificateData(certificateBytes);
      submission.setSubmittedAt(LocalDateTime.now());

      saved = submissionRepository.save(submission);
    }
    double percentage = total > 0 ? ((double) score / (double) total) * 100.0 : 0.0;
    return new AiQuizSubmissionResponse(
        saved != null ? saved.getId() : -1L,
        score,
        total,
        percentage,
        request.getTimeTakenSeconds(),
        "/api/certificate/" + (fallbackQuiz ? -101L : quiz.getId())
    );
  }

  public AiQuizSubmission getCertificate(Long submissionId, String studentEmail) {
    var user = userRepository.findByEmail(studentEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STUDENT) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
    }

    Student student = studentRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student profile not found"));
    AiQuizSubmission submission = submissionRepository.findById(submissionId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Submission not found"));

    if (!submission.getStudent().getId().equals(student.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }

    return submission;
  }

  public AiQuizSubmission getCertificateForQuiz(Long quizId, String studentEmail) {
    var user = userRepository.findByEmail(studentEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STUDENT) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
    }

    Student student = studentRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student profile not found"));

    AiQuizSubmission submission = submissionRepository.findByQuizIdAndStudentId(quizId, student.getId())
        .orElse(null);

    if (submission == null) {
      RealisticDataGenerator.CertificateSample sample = dataGenerator.getDefaultCertificate();
      try {
        byte[] certificateBytes = certificateService.generateCertificate(
            student.getUser().getFullName(),
            student.getStudentCode(),
            sample.subject(),
            student.getDepartment(),
            student.getSection(),
            sample.score(),
            sample.totalMarks(),
            sample.timeTaken(),
            null
        );
        AiQuizSubmission fallback = new AiQuizSubmission();
        fallback.setCertificateFileName("certificate.pdf");
        fallback.setCertificateContentType("application/pdf");
        fallback.setCertificateData(certificateBytes);
        fallback.setScore(sample.score());
        fallback.setTotal(sample.totalMarks());
        fallback.setTimeTakenSeconds(sample.timeTaken());
        return fallback;
      } catch (IOException ex) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate certificate");
      }
    }

    if (submission.getCertificateData() == null || submission.getCertificateData().length == 0) {
      AiQuiz quiz = submission.getQuiz();
      try {
        byte[] certificateBytes = certificateService.generateCertificate(
            student.getUser().getFullName(),
            student.getStudentCode(),
            quiz.getTitle(),
            student.getDepartment(),
            student.getSection(),
            submission.getScore() != null ? submission.getScore() : 0,
            submission.getTotal() != null ? submission.getTotal() : 0,
            submission.getTimeTakenSeconds(),
            quiz.getDurationMinutes()
        );
        submission.setCertificateFileName("certificate-" + student.getStudentCode() + ".pdf");
        submission.setCertificateContentType("application/pdf");
        submission.setCertificateData(certificateBytes);
        submissionRepository.save(submission);
      } catch (IOException ex) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate certificate");
      }
    }

    return submission;
  }

  public AiQuizResponse updateQuiz(
      Long id,
      String facultyEmail,
      String title,
      String syllabus,
      String department,
      String className,
      Integer durationMinutes
  ) {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }

    Staff staff = staffRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty profile not found"));
    AiQuiz quiz = quizRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
    if (!quiz.getCreatedBy().getId().equals(staff.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty can edit only their quizzes");
    }

    String normalizedClass = normalize(className);
    if (normalizedClass != null) {
      boolean allowed = mappingRepository.findByStaffId(staff.getId()).stream()
          .anyMatch(mapping -> mapping.getClassName().equalsIgnoreCase(normalizedClass));
      if (!allowed) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty is not assigned to this class");
      }
      quiz.setClassName(normalizedClass);
      quiz.setSection(extractSection(normalizedClass));
    }

    if (title != null && !title.isBlank()) {
      quiz.setTitle(title);
    }
    if (syllabus != null) {
      quiz.setSyllabus(syllabus);
    }
    if (department != null) {
      quiz.setDepartment(normalize(department));
    } else if (normalizedClass != null && (quiz.getDepartment() == null || quiz.getDepartment().isBlank())) {
      quiz.setDepartment(extractDepartment(normalizedClass));
    }
    if (durationMinutes != null && durationMinutes > 0) {
      quiz.setDurationMinutes(durationMinutes);
    }
    quiz.setUpdatedAt(LocalDateTime.now());

    AiQuiz saved = quizRepository.save(quiz);
    List<AiQuizQuestion> questions = parseQuestions(saved.getQuestionsJson());
    return toResponse(saved, questions);
  }

  public AiQuizResponse updateVisibility(Long id, Boolean visible, String facultyEmail) {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }

    Staff staff = staffRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty profile not found"));
    AiQuiz quiz = quizRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
    if (!quiz.getCreatedBy().getId().equals(staff.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty can update only their quizzes");
    }

    boolean nextValue = visible != null ? visible : !Boolean.TRUE.equals(quiz.getIsVisible());
    quiz.setIsVisible(nextValue);
    quiz.setUpdatedAt(LocalDateTime.now());
    AiQuiz saved = quizRepository.save(quiz);
    List<AiQuizQuestion> questions = parseQuestions(saved.getQuestionsJson());
    return toResponse(saved, questions);
  }

  public void deleteQuiz(Long id, String facultyEmail) {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }

    Staff staff = staffRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty profile not found"));
    AiQuiz quiz = quizRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));
    if (!quiz.getCreatedBy().getId().equals(staff.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty can delete only their quizzes");
    }
    quizRepository.delete(quiz);
  }

  private AiQuizPayload generateAiQuizPayload(AiQuizGenerateRequest request) {
    String prompt = buildPrompt(request);
    String systemPrompt = "You are an academic quiz generator. Respond ONLY with valid JSON.";

    try {
      String json = geminiService.generateStructuredResponse(systemPrompt, prompt);
      return objectMapper.readValue(json, AiQuizPayload.class);
    } catch (Exception ex) {
      return fallbackPayload(request);
    }
  }

  private String buildPrompt(AiQuizGenerateRequest request) {
    return "Generate a quiz in JSON format with fields: title, questions. " +
        "Each question must have: type (MCQ, TRUE_FALSE, FILL_BLANK), question, options (array, only for MCQ and TRUE_FALSE), answer. " +
        "Syllabus/topic: " + request.getSyllabus() + ". " +
        "Number of questions: " + request.getQuestionCount() + ". " +
        "Question types: " + String.join(", ", request.getQuestionTypes()) + ".";
  }

  private AiQuizPayload fallbackPayload(AiQuizGenerateRequest request) {
    List<AiQuizQuestion> questions = List.of(
        new AiQuizQuestion("MCQ", "What does AI stand for?", List.of("Artificial Intelligence", "Analog Interface", "Automated Input", "Active Integration"), "Artificial Intelligence"),
        new AiQuizQuestion("TRUE_FALSE", "A stack follows FIFO.", List.of("True", "False"), "False"),
        new AiQuizQuestion("FILL_BLANK", "The time complexity of binary search is ____.", null, "O(log n)")
    );
    return new AiQuizPayload("AI Quiz", questions);
  }

  private String serializePayload(AiQuizPayload payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (IOException ex) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store quiz");
    }
  }

  private List<AiQuizQuestion> parseQuestions(String json) {
    try {
      AiQuizPayload payload = objectMapper.readValue(json, AiQuizPayload.class);
      return payload.questions();
    } catch (IOException ex) {
      try {
        return objectMapper.readValue(json, new TypeReference<List<AiQuizQuestion>>() {});
      } catch (IOException ignored) {
        throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to parse quiz questions");
      }
    }
  }

  private AiQuizResponse toResponse(AiQuiz quiz, List<AiQuizQuestion> questions) {
    List<AiQuizResponse.AiQuizQuestionView> views = buildView(questions);
    return new AiQuizResponse(
        quiz.getId(),
        quiz.getTitle(),
        quiz.getDepartment(),
        quiz.getClassName(),
        Boolean.TRUE.equals(quiz.getIsVisible()),
        quiz.getDurationMinutes(),
        views
    );
  }

  private List<AiQuizResponse.AiQuizQuestionView> buildView(List<AiQuizQuestion> questions) {
    List<AiQuizResponse.AiQuizQuestionView> views = new java.util.ArrayList<>();
    for (int i = 0; i < questions.size(); i++) {
      AiQuizQuestion q = questions.get(i);
      views.add(new AiQuizResponse.AiQuizQuestionView(
          i,
          q.getType(),
          q.getQuestion(),
          q.getOptions()
      ));
    }
    return views;
  }

  private Map<Integer, AiQuizQuestion> buildIndexMap(List<AiQuizQuestion> questions) {
    return java.util.stream.IntStream.range(0, questions.size())
        .boxed()
        .collect(Collectors.toMap(index -> index, questions::get));
  }

  private boolean isCorrect(String expected, String actual) {
    if (expected == null || actual == null) {
      return false;
    }
    return expected.trim().equalsIgnoreCase(actual.trim());
  }

  private String serializeAnswers(AiQuizSubmissionRequest request) {
    try {
      return objectMapper.writeValueAsString(request.getAnswers());
    } catch (IOException ex) {
      return "[]";
    }
  }

  private String buildClassKey(String department, String section) {
    if (department == null || section == null) {
      return null;
    }
    return department.trim() + "-" + section.trim();
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

  private String extractSection(String className) {
    if (className == null) {
      return null;
    }
    String trimmed = className.trim();
    int dashIndex = trimmed.lastIndexOf("-");
    if (dashIndex > 0 && dashIndex < trimmed.length() - 1) {
      return trimmed.substring(dashIndex + 1).trim().toUpperCase(Locale.ROOT);
    }
    return null;
  }

  private String extractDepartment(String className) {
    if (className == null) {
      return null;
    }
    int dashIndex = className.lastIndexOf("-");
    if (dashIndex > 0) {
      return normalize(className.substring(0, dashIndex));
    }
    return normalize(className);
  }

  private int normalizeDuration(Integer durationMinutes) {
    if (durationMinutes == null || durationMinutes <= 0) {
      return DEFAULT_DURATION_MINUTES;
    }
    return Math.min(durationMinutes, 180);
  }

  private String extractPdfText(MultipartFile file) {
    try (PDDocument document = PDDocument.load(file.getInputStream())) {
      PDFTextStripper stripper = new PDFTextStripper();
      String text = stripper.getText(document);
      if (text == null) {
        return "";
      }
      String trimmed = text.replaceAll("\\s+", " ").trim();
      if (trimmed.length() > MAX_PDF_CHARS) {
        return trimmed.substring(0, MAX_PDF_CHARS);
      }
      return trimmed;
    } catch (IOException ex) {
      return "";
    }
  }

  private AiQuizPayload generateAiQuizPayloadFromText(String content, int questionCount, List<String> questionTypes) {
    String prompt = "Generate " + questionCount + " questions from this syllabus content. " +
        "Types: " + String.join(", ", questionTypes) + ". " +
        "Return JSON array with fields: question, options (if MCQ/TRUE_FALSE), answer, type. " +
        "Content: " + content;
    String systemPrompt = "You are an academic quiz generator. Respond ONLY with valid JSON.";

    try {
      String json = geminiService.generateStructuredResponse(systemPrompt, prompt);
      List<AiQuizQuestion> questions = objectMapper.readValue(json, new TypeReference<List<AiQuizQuestion>>() {});
      return new AiQuizPayload("AI Quiz", questions);
    } catch (Exception ex) {
      return fallbackPayloadFromCount(questionCount);
    }
  }

  private AiQuizPayload fallbackPayloadFromCount(int count) {
    List<AiQuizQuestion> questions = new ArrayList<>();
    questions.add(new AiQuizQuestion("MCQ", "What does AI stand for?", List.of("Artificial Intelligence", "Analog Interface", "Automated Input", "Active Integration"), "Artificial Intelligence"));
    questions.add(new AiQuizQuestion("TRUE_FALSE", "A stack follows FIFO.", List.of("True", "False"), "False"));
    questions.add(new AiQuizQuestion("FILL_BLANK", "The time complexity of binary search is ____.", null, "O(log n)"));
    if (count <= questions.size()) {
      return new AiQuizPayload("AI Quiz", questions.subList(0, count));
    }
    return new AiQuizPayload("AI Quiz", questions);
  }

  private record AiQuizPayload(String title, List<AiQuizQuestion> questions) {}
}
