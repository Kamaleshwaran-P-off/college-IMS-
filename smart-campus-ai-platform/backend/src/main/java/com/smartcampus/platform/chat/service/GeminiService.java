package com.smartcampus.platform.chat.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeminiService {
  private static final Logger log = LoggerFactory.getLogger(GeminiService.class);
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;
  private final String apiKey;
  private final String baseUrl;
  private final String model;

  public GeminiService(
      ObjectMapper objectMapper,
      @Value("${gemini.api-key}") String apiKey,
      @Value("${gemini.base-url:https://generativelanguage.googleapis.com}") String baseUrl,
      @Value("${gemini.model:gemini-pro}") String model
  ) {
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newHttpClient();
    this.apiKey = apiKey;
    this.baseUrl = baseUrl;
    this.model = model;
  }

  public String generateContent(String prompt) {
    if (prompt == null || prompt.isBlank()) {
      return "Please ask a question about assignments, exams, or campus services.";
    }
    String cleanKey = apiKey == null ? "" : apiKey.trim();
    if (cleanKey.isBlank()) {
      throw new IllegalStateException("Gemini API key is not configured");
    }

    Map<String, Object> payload = Map.of(
        "contents",
        List.of(
            Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", prompt))
            )
        )
    );

    String requestBody;
    try {
      requestBody = objectMapper.writeValueAsString(payload);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to serialize Gemini request", ex);
    }

    String cleanBase = baseUrl == null ? "https://generativelanguage.googleapis.com" : baseUrl.trim();
    String cleanModel = model == null || model.isBlank() ? "gemini-1.5-flash" : model.trim();
    String url = cleanBase + "/v1beta/models/" + cleanModel + ":generateContent?key=" + cleanKey;
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(url))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Gemini request interrupted", ex);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to reach Gemini API", ex);
    }

    log.info("Gemini response status: {}", response.statusCode());
    log.debug("Gemini response body: {}", response.body());

    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      throw new IllegalStateException(parseError(response.body()));
    }

    return parseResponse(response.body());
  }

  public String generateStructuredResponse(String systemPrompt, String userPrompt) {
    String systemText = systemPrompt == null ? "" : systemPrompt.strip();
    String userText = userPrompt == null ? "" : userPrompt.strip();
    StringBuilder promptBuilder = new StringBuilder();
    if (!systemText.isBlank()) {
      promptBuilder.append(systemText).append("\n\n");
    }
    promptBuilder.append(userText);
    return generateContent(promptBuilder.toString());
  }

  private String parseResponse(String body) {
    try {
      JsonNode root = objectMapper.readTree(body);
      JsonNode candidates = root.path("candidates");
      if (candidates.isArray() && candidates.size() > 0) {
        JsonNode parts = candidates.get(0).path("content").path("parts");
        if (parts.isArray() && parts.size() > 0) {
          String text = parts.get(0).path("text").asText();
          if (text != null && !text.isBlank()) {
            return text;
          }
        }
      }
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to parse Gemini response", ex);
    }
    return "I'm here to help with LMS-related queries.";
  }

  private String parseError(String body) {
    try {
      JsonNode root = objectMapper.readTree(body);
      String message = root.path("error").path("message").asText();
      if (message != null && !message.isBlank()) {
        return message;
      }
    } catch (IOException ignored) {
      // ignore parse errors
    }
    return "Gemini API error";
  }
}
