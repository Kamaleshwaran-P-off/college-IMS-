package com.smartcampus.platform.chat.service;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.chat.dto.ChatRequest;
import com.smartcampus.platform.chat.dto.ChatResponse;
import com.smartcampus.platform.common.exception.QueryLimitExceededException;
import com.smartcampus.platform.common.exception.ResourceNotFoundException;
import com.smartcampus.platform.quiz.service.QuizService;
import com.smartcampus.platform.queryusage.entity.QueryUsage;
import com.smartcampus.platform.queryusage.repository.QueryUsageRepository;

@Service
@Transactional
public class ChatService {
  private final QueryUsageRepository queryUsageRepository;
  private final UserRepository userRepository;
  private final int dailyLimit;
  private final int bonusQueries;
  private final QuizService quizService;
  private final ChatBonusService chatBonusService;
  private final GeminiService geminiService;

  public ChatService(
      QueryUsageRepository queryUsageRepository,
      UserRepository userRepository,
      QuizService quizService,
      ChatBonusService chatBonusService,
      GeminiService geminiService,
      @Value("${app.chat.daily-limit:5}") int dailyLimit,
      @Value("${app.chat.bonus-queries:5}") int bonusQueries
  ) {
    this.queryUsageRepository = queryUsageRepository;
    this.userRepository = userRepository;
    this.quizService = quizService;
    this.chatBonusService = chatBonusService;
    this.geminiService = geminiService;
    this.dailyLimit = dailyLimit;
    this.bonusQueries = bonusQueries;
  }

  public ChatResponse chat(ChatRequest request) {
    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    LocalDate today = LocalDate.now();
    LocalDateTime start = today.atStartOfDay();
    LocalDateTime end = today.plusDays(1).atStartOfDay();

    long count = queryUsageRepository.countByUserIdAndQueryTypeAndCreatedAtBetween(
        user.getId(),
        "CHAT",
        start,
        end
    );
    boolean unlocked = quizService.hasUnlockedToday(user.getId());
    int rewardBonus = chatBonusService.getBonusForToday(user.getId());
    int effectiveLimit = dailyLimit + (unlocked ? bonusQueries : 0) + rewardBonus;

    if (count >= effectiveLimit) {
      throw new QueryLimitExceededException("UNLOCK_REQUIRED");
    }

    String reply = getBotResponse(request.getMessage());
    int tokensUsed = estimateTokens(request.getMessage(), reply);

    QueryUsage usage = new QueryUsage(
        user,
        "CHAT",
        tokensUsed,
        null
    );
    queryUsageRepository.save(usage);

    int remaining = Math.max(0, effectiveLimit - (int) count - 1);
    return new ChatResponse(reply, remaining, effectiveLimit);
  }

  private String getBotResponse(String message) {
    String text = message == null ? "" : message.trim();
    if (text.isBlank()) {
      return "Please ask a question about assignments, exams, deadlines, or courses.";
    }

    String prompt = "You are an LMS assistant. Answer only about:\n"
        + "- assignments\n"
        + "- exams\n"
        + "- deadlines\n"
        + "- courses\n\n"
        + "User: " + text;

    try {
      return geminiService.generateContent(prompt);
    } catch (Exception ex) {
      return "I'm here to help with LMS-related queries.";
    }
  }

  private int estimateTokens(String prompt, String reply) {
    String combined = (prompt == null ? "" : prompt) + " " + (reply == null ? "" : reply);
    int wordCount = combined.trim().isEmpty() ? 0 : combined.trim().split("\\s+").length;
    return Math.max(1, wordCount);
  }
}
