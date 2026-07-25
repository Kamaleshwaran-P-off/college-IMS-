package com.smartcampus.platform.inbox;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

@Component
public class EmailClassifier {
  private final Map<EmailCategory, List<String>> keywordMap = new LinkedHashMap<>();

  public EmailClassifier() {
    keywordMap.put(EmailCategory.DEADLINES, List.of("deadline", "last date", "due", "submission", "submit"));
    keywordMap.put(EmailCategory.HACKATHONS, List.of("hackathon", "coding event", "codefest", "buildathon"));
    keywordMap.put(EmailCategory.COMPETITIONS, List.of("competition", "contest", "challenge", "tournament"));
    keywordMap.put(EmailCategory.LIBRARY, List.of("library", "book", "return", "renewal"));
    keywordMap.put(EmailCategory.OFFICIAL, List.of("circular", "notice", "meeting", "official", "announcement"));
  }

  public EmailCategory classify(String subject, String content) {
    String haystack = (subject + " " + content).toLowerCase();
    for (Map.Entry<EmailCategory, List<String>> entry : keywordMap.entrySet()) {
      if (entry.getValue().stream().anyMatch(haystack::contains)) {
        return entry.getKey();
      }
    }
    return EmailCategory.UNOFFICIAL;
  }
}
