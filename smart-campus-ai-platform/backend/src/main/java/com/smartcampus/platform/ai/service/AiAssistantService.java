package com.smartcampus.platform.ai.service;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.smartcampus.platform.ai.dto.AiExplainRequest;
import com.smartcampus.platform.ai.dto.AiGenerateQuizRequest;
import com.smartcampus.platform.ai.dto.AiSummarizeRequest;
@Service
public class AiAssistantService {
  public String explainTopic(AiExplainRequest request) {
    String topic = safe(request.getTopic());
    String context = safe(request.getContext());
    StringBuilder builder = new StringBuilder();
    builder.append("**Topic:** ").append(topic).append("\n\n");
    builder.append("**Quick explanation:**\n");
    builder.append("- Definition: ").append(topic).append(" refers to the core ideas and concepts you should master.\n");
    builder.append("- Why it matters: It appears in assessments and helps with real-world applications.\n");
    builder.append("- How to study: Review notes, practice problems, and summarize key formulas.\n");
    if (!context.isBlank()) {
      builder.append("- Context note: ").append(context).append("\n");
    }
    builder.append("\n**Next step:** Try solving 2-3 questions related to this topic.");
    return builder.toString();
  }

  public String summarizeContent(AiSummarizeRequest request) {
    String content = safe(request.getContent());
    String snippet = content.length() > 450 ? content.substring(0, 450) + "..." : content;
    String[] sentences = snippet.split("(?<=[.!?])\\s+");
    StringBuilder builder = new StringBuilder();
    builder.append("**Summary:**\n");
    for (int i = 0; i < sentences.length && i < 3; i++) {
      builder.append("- ").append(sentences[i].trim()).append("\n");
    }
    if (sentences.length == 0) {
      builder.append("- Key concepts and definitions are highlighted.\n");
    }
    builder.append("\n**Takeaway:** Focus on the main terms, processes, and examples.");
    return builder.toString();
  }

  public String generateQuiz(AiGenerateQuizRequest request) {
    String topic = safe(request.getTopic());
    int count = request.getQuestionCount() == null ? 8 : request.getQuestionCount();
    List<String> types = request.getQuestionTypes() == null || request.getQuestionTypes().isEmpty()
        ? List.of("MCQ", "TRUE_FALSE", "FILL_BLANK")
        : request.getQuestionTypes();

    String normalizedTypes = types.stream()
        .map(type -> type.replace("_", " ").toUpperCase(Locale.ENGLISH))
        .reduce((a, b) -> a + ", " + b)
        .orElse("MCQ");

    StringBuilder builder = new StringBuilder();
    builder.append("**Quiz: ").append(topic).append("**\n");
    builder.append("_Question types: ").append(normalizedTypes).append("_\n\n");

    for (int i = 1; i <= count; i++) {
      String type = types.get((i - 1) % types.size());
      builder.append(i).append(". (").append(type.replace("_", " ")).append(") ")
          .append("Explain the key idea of ").append(topic).append(".\n");
      builder.append("   **Answer:** Focus on definition, purpose, and one example.\n\n");
    }

    return builder.toString();
  }

  private String safe(String value) {
    if (!StringUtils.hasText(value)) {
      return "General studies";
    }
    return value.trim();
  }
}
