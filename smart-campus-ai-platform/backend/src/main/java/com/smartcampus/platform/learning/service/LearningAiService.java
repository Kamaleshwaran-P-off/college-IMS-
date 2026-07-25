package com.smartcampus.platform.learning.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.smartcampus.platform.learning.dto.QuizQuestionDto;

@Service
public class LearningAiService {
  public LearningAiService() {}

  public List<TopicSeed> generateTopics(String extractedText) {
    return fallbackTopics(extractedText);
  }

  public List<QuizQuestionDto> generateQuiz(String topicTitle, String topicContent) {
    return fallbackQuiz(topicTitle);
  }

  public String chatOnTopic(String topicTitle, String topicContent, String message) {
    String snippet = truncate(topicContent, 240);
    return "Topic: " + topicTitle + "\n" +
        "Quick help: Review the key definitions and examples first. " +
        "Then answer your question by relating it to: " + (snippet.isBlank() ? "the main concept." : snippet);
  }

  private List<TopicSeed> fallbackTopics(String text) {
    String cleaned = text == null ? "" : text.trim();
    if (cleaned.isEmpty()) {
      return List.of(
          new TopicSeed("Overview", "Introduction to the material"),
          new TopicSeed("Key Concepts", "Core ideas and definitions"),
          new TopicSeed("Examples", "Worked examples and applications"),
          new TopicSeed("Summary", "Key takeaways and revision points"),
          new TopicSeed("Practice", "Self-check questions")
      );
    }
    String[] sentences = cleaned.split("(?<=[.!?])\\s+");
    List<TopicSeed> topics = new ArrayList<>();
    for (int i = 0; i < sentences.length && topics.size() < 5; i++) {
      String sentence = sentences[i].trim();
      if (sentence.length() > 15) {
        topics.add(new TopicSeed("Topic " + (topics.size() + 1), sentence.substring(0, Math.min(140, sentence.length()))));
      }
    }
    if (topics.isEmpty()) {
      topics.add(new TopicSeed("Overview", "Introduction to the material"));
    }
    return topics;
  }

  private List<QuizQuestionDto> fallbackQuiz(String topicTitle) {
    return List.of(
        new QuizQuestionDto(1, "TRUE_FALSE", "The topic \"" + topicTitle + "\" has core definitions to memorize.", List.of("True", "False"), "True", "Most academic topics have key definitions to remember."),
        new QuizQuestionDto(2, "MCQ", "Which activity helps master \"" + topicTitle + "\"?", List.of("Reading", "Practice questions", "Summaries", "All of the above"), "All of the above", "Combining study methods improves retention."),
        new QuizQuestionDto(3, "FILL_BLANK", "Consistent review improves ______ retention.", List.of(), "memory", "Regular revision strengthens memory.")
    );
  }

  private String truncate(String text, int maxChars) {
    if (!StringUtils.hasText(text)) return "";
    return text.length() <= maxChars ? text : text.substring(0, maxChars);
  }

  public record TopicSeed(String title, String description) {}
}
