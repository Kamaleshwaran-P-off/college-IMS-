package com.smartcampus.platform.gmail.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartcampus.platform.gmail.entity.GmailToken;
import com.smartcampus.platform.gmail.repository.GmailTokenRepository;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;

@Service
public class GmailOAuthService {
  private final GmailTokenRepository tokenRepository;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient;

  private final String clientId;
  private final String clientSecret;
  private final String redirectUri;
  private final String uiRedirect;

  public GmailOAuthService(
      GmailTokenRepository tokenRepository,
      ObjectMapper objectMapper,
      @Value("${app.gmail.client-id:}") String clientId,
      @Value("${app.gmail.client-secret:}") String clientSecret,
      @Value("${app.gmail.redirect-uri:}") String redirectUri,
      @Value("${app.gmail.ui-redirect:http://localhost:5173/inbox}") String uiRedirect
  ) {
    this.tokenRepository = tokenRepository;
    this.objectMapper = objectMapper;
    this.httpClient = HttpClient.newHttpClient();
    this.clientId = clientId;
    this.clientSecret = clientSecret;
    this.redirectUri = redirectUri;
    this.uiRedirect = uiRedirect;
  }

  public String buildAuthUrl(Long userId) {
    if (clientId == null || clientId.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gmail client ID not configured");
    }
    if (redirectUri == null || redirectUri.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Gmail redirect URI not configured");
    }
    String state = Base64.getUrlEncoder().encodeToString(("user:" + userId).getBytes(StandardCharsets.UTF_8));
    String scope = URLEncoder.encode("https://www.googleapis.com/auth/gmail.readonly", StandardCharsets.UTF_8);
    return "https://accounts.google.com/o/oauth2/v2/auth" +
        "?client_id=" + url(clientId) +
        "&redirect_uri=" + url(redirectUri) +
        "&response_type=code" +
        "&access_type=offline" +
        "&prompt=consent" +
        "&scope=" + scope +
        "&state=" + url(state);
  }

  public String handleCallback(String code, String state) {
    Long userId = parseState(state);
    TokenResponse tokenResponse = exchangeCode(code);

    GmailToken token = tokenRepository.findByUserId(userId)
        .orElse(new GmailToken());
    token.setUserId(userId);
    token.setAccessToken(tokenResponse.accessToken());
    if (tokenResponse.refreshToken() != null && !tokenResponse.refreshToken().isBlank()) {
      token.setRefreshToken(tokenResponse.refreshToken());
    }
    token.setScope(tokenResponse.scope());
    token.setExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.expiresIn() - 60));
    token.setCreatedAt(LocalDateTime.now());
    tokenRepository.save(token);

    return uiRedirect + "?gmail=linked";
  }

  public GmailToken refreshTokenIfNeeded(GmailToken token) {
    if (token.getExpiresAt() != null && token.getExpiresAt().isAfter(LocalDateTime.now().plusSeconds(30))) {
      return token;
    }
    if (token.getRefreshToken() == null || token.getRefreshToken().isBlank()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "GMAIL_REFRESH_MISSING");
    }

    TokenResponse response = refreshAccessToken(token.getRefreshToken());
    token.setAccessToken(response.accessToken());
    token.setScope(response.scope());
    token.setExpiresAt(LocalDateTime.now().plusSeconds(response.expiresIn() - 60));
    return tokenRepository.save(token);
  }

  public String getUiRedirect() {
    return uiRedirect;
  }

  private TokenResponse exchangeCode(String code) {
    String body = "code=" + url(code) +
        "&client_id=" + url(clientId) +
        "&client_secret=" + url(clientSecret) +
        "&redirect_uri=" + url(redirectUri) +
        "&grant_type=authorization_code";

    return callTokenEndpoint(body);
  }

  private TokenResponse refreshAccessToken(String refreshToken) {
    String body = "refresh_token=" + url(refreshToken) +
        "&client_id=" + url(clientId) +
        "&client_secret=" + url(clientSecret) +
        "&grant_type=refresh_token";

    return callTokenEndpoint(body);
  }

  private TokenResponse callTokenEndpoint(String body) {
    try {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create("https://oauth2.googleapis.com/token"))
          .header("Content-Type", "application/x-www-form-urlencoded")
          .POST(HttpRequest.BodyPublishers.ofString(body))
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() < 200 || response.statusCode() >= 300) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "GMAIL_TOKEN_EXCHANGE_FAILED");
      }
      JsonNode root = objectMapper.readTree(response.body());
      return new TokenResponse(
          root.path("access_token").asText(),
          root.path("refresh_token").asText(null),
          root.path("scope").asText(),
          root.path("expires_in").asLong(3600)
      );
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "GMAIL_TOKEN_EXCHANGE_FAILED", ex);
    }
  }

  private Long parseState(String state) {
    if (state == null) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing state");
    }
    try {
      String decoded = new String(Base64.getUrlDecoder().decode(state), StandardCharsets.UTF_8);
      if (decoded.startsWith("user:")) {
        return Long.parseLong(decoded.substring(5));
      }
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid state");
    } catch (Exception ex) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid state", ex);
    }
  }

  private String url(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }

  public record TokenResponse(String accessToken, String refreshToken, String scope, long expiresIn) {}
}
