package com.smartcampus.platform.gmail.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.platform.gmail.dto.GmailEmailResponse;

@Service
public class GmailApiService {
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  public GmailApiService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newHttpClient();
  }

  public List<GmailEmailResponse> fetchEmails(String accessToken, int maxResults) {
    String listUrl = "https://gmail.googleapis.com/gmail/v1/users/me/messages?maxResults=" + maxResults;
    JsonNode list = getJson(listUrl, accessToken);
    List<GmailEmailResponse> results = new ArrayList<>();
    if (list.path("messages").isArray()) {
      for (JsonNode msg : list.path("messages")) {
        String id = msg.path("id").asText();
        if (id == null || id.isBlank()) continue;
        GmailEmailResponse email = fetchMessage(accessToken, id);
        if (email != null) {
          results.add(email);
        }
      }
    }
    return results;
  }

  private GmailEmailResponse fetchMessage(String accessToken, String messageId) {
    String url = "https://gmail.googleapis.com/gmail/v1/users/me/messages/" + messageId + "?format=metadata&metadataHeaders=Subject&metadataHeaders=From&metadataHeaders=Date";
    JsonNode message = getJson(url, accessToken);
    String subject = headerValue(message, "Subject");
    String sender = headerValue(message, "From");
    String date = headerValue(message, "Date");
    String snippet = message.path("snippet").asText("");
    String formattedDate = formatDate(date);
    return new GmailEmailResponse(messageId, subject, sender, snippet, formattedDate, null, null);
  }

  private String headerValue(JsonNode message, String header) {
    JsonNode headers = message.path("payload").path("headers");
    if (headers.isArray()) {
      for (JsonNode node : headers) {
        if (header.equalsIgnoreCase(node.path("name").asText())) {
          return node.path("value").asText("");
        }
      }
    }
    return "";
  }

  private String formatDate(String raw) {
    if (raw == null || raw.isBlank()) return "";
    try {
      Instant instant = Instant.parse(raw);
      return DateTimeFormatter.ofPattern("MMM dd, yyyy").withZone(ZoneId.systemDefault()).format(instant);
    } catch (Exception ex) {
      return raw;
    }
  }

  private JsonNode getJson(String url, String accessToken) {
    try {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .header("Authorization", "Bearer " + accessToken)
          .GET()
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 401) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "GMAIL_TOKEN_EXPIRED");
      }
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "GMAIL_API_ERROR");
      }
      return objectMapper.readTree(response.body());
    } catch (ResponseStatusException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "GMAIL_API_ERROR", ex);
    }
  }
}
