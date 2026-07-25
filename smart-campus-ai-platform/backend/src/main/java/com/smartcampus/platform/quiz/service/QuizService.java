package com.smartcampus.platform.quiz.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.common.exception.ResourceNotFoundException;
import com.smartcampus.platform.quiz.dto.QuizAnswerRequest;
import com.smartcampus.platform.quiz.dto.QuizQuestionResponse;
import com.smartcampus.platform.quiz.dto.QuizQuestionsResponse;
import com.smartcampus.platform.quiz.dto.QuizSubmissionRequest;
import com.smartcampus.platform.quiz.dto.QuizSubmissionResponse;
import com.smartcampus.platform.quiz.entity.QuizAttempt;
import com.smartcampus.platform.quiz.entity.QuizCategory;
import com.smartcampus.platform.quiz.entity.QuizOption;
import com.smartcampus.platform.quiz.entity.QuizQuestion;
import com.smartcampus.platform.quiz.repository.QuizAttemptRepository;
import com.smartcampus.platform.quiz.repository.QuizQuestionRepository;
import com.smartcampus.platform.notification.entity.NotificationType;
import com.smartcampus.platform.notification.service.NotificationService;

@Service
@Transactional
public class QuizService {
  private final QuizQuestionRepository questionRepository;
  private final QuizAttemptRepository attemptRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;
  private final int timeLimitSeconds;
  private final int bonusQueries;
  private final Random random = new Random();

  public QuizService(
      QuizQuestionRepository questionRepository,
      QuizAttemptRepository attemptRepository,
      UserRepository userRepository,
      NotificationService notificationService,
      @Value("${app.quiz.time-limit-seconds:180}") int timeLimitSeconds,
      @Value("${app.chat.bonus-queries:5}") int bonusQueries
  ) {
    this.questionRepository = questionRepository;
    this.attemptRepository = attemptRepository;
    this.userRepository = userRepository;
    this.notificationService = notificationService;
    this.timeLimitSeconds = timeLimitSeconds;
    this.bonusQueries = bonusQueries;
  }

  public QuizQuestionsResponse getQuestions() {
    QuizQuestion aptitude = pickRandom(questionRepository.findByCategory(QuizCategory.APTITUDE), "APTITUDE");
    QuizQuestion dsa = pickRandom(questionRepository.findByCategory(QuizCategory.DSA), "DSA");

    List<QuizQuestionResponse> questions = List.of(toResponse(aptitude), toResponse(dsa));
    return new QuizQuestionsResponse(questions, timeLimitSeconds);
  }

  public QuizSubmissionResponse submit(QuizSubmissionRequest request) {
    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (request.getAnswers() == null || request.getAnswers().size() != 2) {
      throw new IllegalArgumentException("Exactly two answers are required");
    }

    QuizAnswerRequest first = request.getAnswers().get(0);
    QuizAnswerRequest second = request.getAnswers().get(1);

    QuizQuestion q1 = questionRepository.findById(first.getQuestionId())
        .orElseThrow(() -> new ResourceNotFoundException("Question not found"));
    QuizQuestion q2 = questionRepository.findById(second.getQuestionId())
        .orElseThrow(() -> new ResourceNotFoundException("Question not found"));

    if (q1.getCategory() == q2.getCategory()) {
      throw new IllegalArgumentException("Answers must include one aptitude and one DSA question");
    }

    int correctCount = 0;
    if (q1.getCorrectOption() == first.getSelectedOption()) {
      correctCount++;
    }
    if (q2.getCorrectOption() == second.getSelectedOption()) {
      correctCount++;
    }

    boolean passed = correctCount == 2;

    QuizQuestion aptitude = q1.getCategory() == QuizCategory.APTITUDE ? q1 : q2;
    QuizQuestion dsa = q1.getCategory() == QuizCategory.DSA ? q1 : q2;
    QuizOption aptitudeAnswer = q1.getCategory() == QuizCategory.APTITUDE ? first.getSelectedOption() : second.getSelectedOption();
    QuizOption dsaAnswer = q1.getCategory() == QuizCategory.DSA ? first.getSelectedOption() : second.getSelectedOption();

    QuizAttempt attempt = new QuizAttempt(
        user,
        aptitude,
        dsa,
        aptitudeAnswer,
        dsaAnswer,
        correctCount,
        passed
    );
    attemptRepository.save(attempt);

    String message = passed
        ? "Unlocked extra queries for today"
        : "Quiz failed. Answer both correctly to unlock.";

    if (passed) {
      notificationService.createNotification(
          user.getId(),
          NotificationType.REWARD,
          "Quiz reward",
          "You unlocked +" + bonusQueries + " chatbot queries today."
      );
    }

    return new QuizSubmissionResponse(passed, correctCount, 2, message, passed ? bonusQueries : 0);
  }

  public boolean hasUnlockedToday(Long userId) {
    LocalDate today = LocalDate.now();
    LocalDateTime start = today.atStartOfDay();
    LocalDateTime end = today.plusDays(1).atStartOfDay();
    return attemptRepository.existsByUserIdAndPassedTrueAndCreatedAtBetween(userId, start, end);
  }

  private QuizQuestionResponse toResponse(QuizQuestion question) {
    return new QuizQuestionResponse(
        question.getId(),
        question.getCategory(),
        question.getQuestion(),
        question.getOptionA(),
        question.getOptionB(),
        question.getOptionC(),
        question.getOptionD()
    );
  }

  private QuizQuestion pickRandom(List<QuizQuestion> questions, String label) {
    if (questions == null || questions.isEmpty()) {
      throw new IllegalStateException("No " + label + " questions available");
    }
    return questions.get(random.nextInt(questions.size()));
  }
}
