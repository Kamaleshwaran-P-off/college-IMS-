package com.smartcampus.platform.risk.service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.platform.risk.dto.RiskPredictionRequest;
import com.smartcampus.platform.risk.dto.RiskPredictionResponse;

@Service
public class RiskPredictionService {
  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;
  private final String baseUrl;

  public RiskPredictionService(
      ObjectMapper objectMapper,
      @Value("${app.risk-service.base-url:http://localhost:8001}") String baseUrl
  ) {
    this.httpClient = HttpClient.newHttpClient();
    this.objectMapper = objectMapper;
    this.baseUrl = baseUrl;
  }

  public RiskPredictionResponse predict(RiskPredictionRequest request) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("marks", request.getMarks() != null ? request.getMarks() : List.of());
    payload.put("attendance", request.getAttendance() != null ? request.getAttendance() : 0.0);
    payload.put("assignment_completion", request.getAssignmentCompletion() != null ? request.getAssignmentCompletion() : 0.0);

    String body;
    try {
      body = objectMapper.writeValueAsString(payload);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to serialize risk request", ex);
    }

    HttpRequest httpRequest = HttpRequest.newBuilder()
        .uri(URI.create(baseUrl + "/predict-risk"))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body))
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Risk service request interrupted", ex);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to reach risk service", ex);
    }

    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      throw new IllegalStateException("Risk service error: " + response.body());
    }

    try {
      JsonNode root = objectMapper.readTree(response.body());
      String risk = root.path("risk").asText("MEDIUM");
      double score = root.path("score").asDouble(0.0);
      return new RiskPredictionResponse(risk, score);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to parse risk response", ex);
    }
  }
}
