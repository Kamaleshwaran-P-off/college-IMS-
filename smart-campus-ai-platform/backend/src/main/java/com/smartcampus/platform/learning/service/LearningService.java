package com.smartcampus.platform.learning.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.platform.learning.dto.LearningCourseDetailResponse;
import com.smartcampus.platform.learning.dto.LearningCourseSummary;
import com.smartcampus.platform.learning.dto.LearningProgressResponse;
import com.smartcampus.platform.learning.dto.LearningTopicResponse;
import com.smartcampus.platform.learning.dto.QuizQuestionDto;
import com.smartcampus.platform.learning.dto.QuizResponse;
import com.smartcampus.platform.learning.dto.QuizSubmitRequest;
import com.smartcampus.platform.learning.dto.QuizSubmitResponse;
import com.smartcampus.platform.learning.entity.LearningCourse;
import com.smartcampus.platform.learning.entity.LearningProfile;
import com.smartcampus.platform.learning.entity.LearningQuiz;
import com.smartcampus.platform.learning.entity.LearningQuizAttempt;
import com.smartcampus.platform.learning.entity.LearningTopic;
import com.smartcampus.platform.learning.entity.LearningTopicProgress;
import com.smartcampus.platform.learning.entity.TopicStatus;
import com.smartcampus.platform.learning.repository.LearningCourseRepository;
import com.smartcampus.platform.learning.repository.LearningProfileRepository;
import com.smartcampus.platform.learning.repository.LearningQuizAttemptRepository;
import com.smartcampus.platform.learning.repository.LearningQuizRepository;
import com.smartcampus.platform.learning.repository.LearningTopicProgressRepository;
import com.smartcampus.platform.learning.repository.LearningTopicRepository;
import com.smartcampus.platform.defaultdata.RealisticDataGenerator;

@Service
public class LearningService {
  private final LearningCourseRepository courseRepository;
  private final LearningTopicRepository topicRepository;
  private final LearningTopicProgressRepository progressRepository;
  private final LearningQuizRepository quizRepository;
  private final LearningQuizAttemptRepository attemptRepository;
  private final LearningProfileRepository profileRepository;
  private final LearningAiService learningAiService;
  private final ObjectMapper objectMapper;
  private final RealisticDataGenerator dataGenerator;

  public LearningService(
      LearningCourseRepository courseRepository,
      LearningTopicRepository topicRepository,
      LearningTopicProgressRepository progressRepository,
      LearningQuizRepository quizRepository,
      LearningQuizAttemptRepository attemptRepository,
      LearningProfileRepository profileRepository,
      LearningAiService learningAiService,
      ObjectMapper objectMapper,
      RealisticDataGenerator dataGenerator
  ) {
    this.courseRepository = courseRepository;
    this.topicRepository = topicRepository;
    this.progressRepository = progressRepository;
    this.quizRepository = quizRepository;
    this.attemptRepository = attemptRepository;
    this.profileRepository = profileRepository;
    this.learningAiService = learningAiService;
    this.objectMapper = objectMapper;
    this.dataGenerator = dataGenerator;
  }

  public LearningCourse createCourse(Long facultyId, String title, String description, MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "PDF file is required");
    }

    String extractedText = extractPdfText(file);
    LearningCourse course = new LearningCourse(
        title,
        description,
        facultyId,
        file.getOriginalFilename(),
        file.getContentType(),
        getBytes(file),
        extractedText,
        LocalDateTime.now()
    );
    LearningCourse savedCourse = courseRepository.save(course);

    List<LearningAiService.TopicSeed> topics = learningAiService.generateTopics(extractedText);
    int order = 1;
    for (LearningAiService.TopicSeed seed : topics) {
      LearningTopic topic = new LearningTopic(savedCourse, seed.title(), seed.description(), order, seed.description());
      topicRepository.save(topic);
      savedCourse.getTopics().add(topic);
      order += 1;
    }
    return savedCourse;
  }

  public List<LearningCourseSummary> listFacultyCourses(Long facultyId) {
    List<LearningCourseSummary> courses = courseRepository.findByFacultyIdOrderByCreatedAtDesc(facultyId)
        .stream()
        .map(course -> new LearningCourseSummary(
            course.getId(),
            course.getTitle(),
            course.getDescription(),
            course.getCreatedAt(),
            course.getTopics().size()
        ))
        .toList();
    return courses.isEmpty() ? dataGenerator.getDefaultLearningCourses() : courses;
  }

  public List<LearningCourseSummary> listStudentCourses() {
    try {
      List<LearningCourseSummary> courses = courseRepository.findAll()
          .stream()
          .sorted(Comparator.comparing(LearningCourse::getCreatedAt).reversed())
          .map(course -> new LearningCourseSummary(
              course.getId(),
              course.getTitle(),
              course.getDescription(),
              course.getCreatedAt(),
              course.getTopics().size()
          ))
          .toList();
      return courses.isEmpty() ? dataGenerator.getDefaultLearningCourses() : courses;
    } catch (Exception ex) {
      return dataGenerator.getDefaultLearningCourses();
    }
  }

  public LearningCourseDetailResponse getCourseTopics(Long studentId, Long courseId) {
    try {
      LearningCourse course = courseRepository.findById(courseId)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
      List<LearningTopic> topics = topicRepository.findByCourseIdOrderByTopicOrderAsc(courseId);
      if (topics.isEmpty()) {
        return dataGenerator.getDefaultLearningCourseDetail(courseId);
      }
      List<LearningTopicProgress> progress = initProgress(studentId, topics);

      Map<Long, LearningTopicProgress> progressMap = progress.stream()
          .collect(Collectors.toMap(p -> p.getTopic().getId(), Function.identity()));

      List<LearningTopicResponse> topicResponses = topics.stream()
          .map(topic -> {
            LearningTopicProgress entry = progressMap.get(topic.getId());
            String status = entry == null ? TopicStatus.LOCKED.name() : entry.getStatus().name();
            Integer bestScore = entry == null ? null : entry.getBestScore();
            return new LearningTopicResponse(topic.getId(), topic.getTitle(), topic.getDescription(), topic.getTopicOrder(), status, bestScore);
          })
          .toList();

      return new LearningCourseDetailResponse(course.getId(), course.getTitle(), course.getDescription(), topicResponses);
    } catch (Exception ex) {
      return dataGenerator.getDefaultLearningCourseDetail(courseId);
    }
  }

  public QuizResponse getQuizForTopic(Long studentId, Long topicId) {
    LearningTopic topic = topicRepository.findById(topicId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));

    LearningTopicProgress progress = progressRepository.findByStudentIdAndTopicId(studentId, topicId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Topic not unlocked"));

    if (progress.getStatus() == TopicStatus.LOCKED) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Topic is locked");
    }

    LearningQuiz quiz = quizRepository.findByTopicId(topicId)
        .orElseGet(() -> {
          String source = topic.getCourse() != null ? topic.getCourse().getExtractedText() : topic.getContent();
          List<QuizQuestionDto> questions = learningAiService.generateQuiz(topic.getTitle(), source);
          String json = serializeQuestions(questions);
          LearningQuiz saved = new LearningQuiz(topic, json, LocalDateTime.now());
          return quizRepository.save(saved);
        });

    List<QuizQuestionDto> questions = deserializeQuestions(quiz.getQuestionsJson());
    return new QuizResponse(topicId, questions);
  }

  public QuizSubmitResponse submitQuiz(Long studentId, Long topicId, QuizSubmitRequest request) {
    LearningTopic topic = topicRepository.findById(topicId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));
    LearningQuiz quiz = quizRepository.findByTopicId(topicId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Quiz not found"));

    List<QuizQuestionDto> questions = deserializeQuestions(quiz.getQuestionsJson());
    List<String> answers = request.getAnswers() == null ? List.of() : request.getAnswers();

    int score = 0;
    List<String> explanations = new ArrayList<>();
    for (int i = 0; i < questions.size(); i++) {
      QuizQuestionDto question = questions.get(i);
      String provided = i < answers.size() ? safe(answers.get(i)) : "";
      String expected = safe(question.getAnswer());
      if (!expected.isBlank() && expected.equalsIgnoreCase(provided.trim())) {
        score += 1;
      }
      explanations.add(question.getExplanation());
    }

    boolean passed = score >= 2;
    attemptRepository.save(new LearningQuizAttempt(studentId, topic, score, passed, serializeAnswers(answers), LocalDateTime.now()));

    LearningTopicProgress progress = progressRepository.findByStudentIdAndTopicId(studentId, topicId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Topic not unlocked"));

    progress.setAttempts(progress.getAttempts() == null ? 1 : progress.getAttempts() + 1);
    progress.setBestScore(progress.getBestScore() == null ? score : Math.max(progress.getBestScore(), score));
    progress.setUpdatedAt(LocalDateTime.now());
    if (passed) {
      progress.setStatus(TopicStatus.COMPLETED);
    }
    progressRepository.save(progress);

    Long nextTopicId = null;
    if (passed) {
      nextTopicId = unlockNextTopic(studentId, topic.getCourse().getId(), topic.getTopicOrder());
      updateGamification(studentId, score);
    }

    return new QuizSubmitResponse(score, passed, nextTopicId, explanations);
  }

  public LearningProgressResponse getProgress(Long studentId) {
    try {
      List<LearningTopicProgress> progressList = progressRepository.findByStudentId(studentId);
      int totalTopics = progressList.size();
      if (totalTopics == 0) {
        totalTopics = (int) topicRepository.count();
      }
      if (totalTopics == 0) {
        return dataGenerator.getDefaultLearningProgress();
      }
      int completed = (int) progressList.stream().filter(p -> p.getStatus() == TopicStatus.COMPLETED).count();
      double percent = totalTopics == 0 ? 0 : (completed * 100.0) / totalTopics;

      LearningProfile profile = profileRepository.findByStudentId(studentId)
          .orElseGet(() -> profileRepository.save(new LearningProfile(studentId, 0, 0, LocalDate.now(), "")));

      List<Integer> recentScores = attemptRepository.findAll()
          .stream()
          .filter(attempt -> attempt.getStudentId().equals(studentId))
          .sorted(Comparator.comparing(LearningQuizAttempt::getCreatedAt).reversed())
          .limit(5)
          .map(LearningQuizAttempt::getScore)
          .toList();

      return new LearningProgressResponse(completed, totalTopics, percent, profile.getPoints(), profile.getStreak(), profile.getBadges(), recentScores);
    } catch (Exception ex) {
      return dataGenerator.getDefaultLearningProgress();
    }
  }

  public String chatOnTopic(Long topicId, String message) {
    LearningTopic topic = topicRepository.findById(topicId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));
    String source = topic.getCourse() != null ? topic.getCourse().getExtractedText() : topic.getContent();
    return learningAiService.chatOnTopic(topic.getTitle(), source, message);
  }

  private List<LearningTopicProgress> initProgress(Long studentId, List<LearningTopic> topics) {
    if (topics.isEmpty()) return List.of();
    List<LearningTopicProgress> existing = progressRepository.findByStudentIdAndTopicCourseId(studentId, topics.get(0).getCourse().getId());
    Map<Long, LearningTopicProgress> map = existing.stream()
        .collect(Collectors.toMap(p -> p.getTopic().getId(), Function.identity()));

    int maxCompletedOrder = existing.stream()
        .filter(p -> p.getStatus() == TopicStatus.COMPLETED)
        .map(p -> p.getTopic().getTopicOrder())
        .max(Integer::compareTo)
        .orElse(0);
    int nextOrder = maxCompletedOrder + 1;

    List<LearningTopicProgress> updated = new ArrayList<>();
    for (LearningTopic topic : topics) {
      LearningTopicProgress progress = map.get(topic.getId());
      if (progress == null) {
        TopicStatus status;
        if (topic.getTopicOrder() == 1 && maxCompletedOrder == 0) {
          status = TopicStatus.UNLOCKED;
        } else if (topic.getTopicOrder() == nextOrder) {
          status = TopicStatus.UNLOCKED;
        } else {
          status = TopicStatus.LOCKED;
        }
        progress = new LearningTopicProgress(studentId, topic, status, null, 0, LocalDateTime.now());
      } else if (progress.getStatus() == TopicStatus.LOCKED && topic.getTopicOrder() == nextOrder) {
        progress.setStatus(TopicStatus.UNLOCKED);
        progress.setUpdatedAt(LocalDateTime.now());
      }
      updated.add(progressRepository.save(progress));
    }
    return updated;
  }

  private Long unlockNextTopic(Long studentId, Long courseId, int currentOrder) {
    List<LearningTopic> topics = topicRepository.findByCourseIdOrderByTopicOrderAsc(courseId);
    Optional<LearningTopic> nextTopic = topics.stream()
        .filter(topic -> topic.getTopicOrder() == currentOrder + 1)
        .findFirst();
    if (nextTopic.isEmpty()) return null;

    LearningTopic topic = nextTopic.get();
    LearningTopicProgress progress = progressRepository.findByStudentIdAndTopicId(studentId, topic.getId())
        .orElse(new LearningTopicProgress(studentId, topic, TopicStatus.LOCKED, null, 0, LocalDateTime.now()));
    progress.setStatus(TopicStatus.UNLOCKED);
    progress.setUpdatedAt(LocalDateTime.now());
    progressRepository.save(progress);
    return topic.getId();
  }

  private void updateGamification(Long studentId, int score) {
    LearningProfile profile = profileRepository.findByStudentId(studentId)
        .orElseGet(() -> new LearningProfile(studentId, 0, 0, null, ""));
    profile.setPoints(profile.getPoints() + (score * 10));

    LocalDate today = LocalDate.now();
    LocalDate lastActive = profile.getLastActive();
    if (lastActive == null || lastActive.equals(today.minusDays(1))) {
      profile.setStreak(profile.getStreak() + 1);
    } else if (!lastActive.equals(today)) {
      profile.setStreak(1);
    }
    profile.setLastActive(today);

    if (profile.getPoints() >= 100 && (profile.getBadges() == null || !profile.getBadges().contains("Rising Star"))) {
      profile.setBadges(profile.getBadges() == null || profile.getBadges().isBlank()
          ? "Rising Star"
          : profile.getBadges() + ",Rising Star");
    }
    profileRepository.save(profile);
  }

  private String extractPdfText(MultipartFile file) {
    try (PDDocument document = PDDocument.load(file.getBytes())) {
      PDFTextStripper stripper = new PDFTextStripper();
      return stripper.getText(document);
    } catch (IOException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to read PDF", ex);
    }
  }

  private byte[] getBytes(MultipartFile file) {
    try {
      return file.getBytes();
    } catch (IOException ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unable to read file bytes", ex);
    }
  }

  private String serializeQuestions(List<QuizQuestionDto> questions) {
    try {
      return objectMapper.writeValueAsString(questions);
    } catch (JsonProcessingException ex) {
      throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to serialize quiz", ex);
    }
  }

  private List<QuizQuestionDto> deserializeQuestions(String json) {
    if (json == null || json.isBlank()) return List.of();
    try {
      return objectMapper.readValue(json, objectMapper.getTypeFactory().constructCollectionType(List.class, QuizQuestionDto.class));
    } catch (IOException ex) {
      return List.of();
    }
  }

  private String serializeAnswers(List<String> answers) {
    try {
      return objectMapper.writeValueAsString(answers);
    } catch (JsonProcessingException ex) {
      return "[]";
    }
  }

  private String safe(String value) {
    return value == null ? "" : value.trim();
  }
}
