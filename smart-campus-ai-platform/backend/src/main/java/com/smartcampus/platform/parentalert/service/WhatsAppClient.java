package com.smartcampus.platform.parentalert.service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WhatsAppClient {
  private final String provider;
  private final String accountSid;
  private final String authToken;
  private final String fromNumber;
  private final HttpClient httpClient = HttpClient.newHttpClient();

  public WhatsAppClient(
      @Value("${app.whatsapp.provider:twilio}") String provider,
      @Value("${app.whatsapp.twilio.account-sid:}") String accountSid,
      @Value("${app.whatsapp.twilio.auth-token:}") String authToken,
      @Value("${app.whatsapp.twilio.from-number:}") String fromNumber
  ) {
    this.provider = provider;
    this.accountSid = accountSid;
    this.authToken = authToken;
    this.fromNumber = fromNumber;
  }

  public boolean isConfigured() {
    return "twilio".equalsIgnoreCase(provider)
        && accountSid != null && !accountSid.isBlank()
        && authToken != null && !authToken.isBlank()
        && fromNumber != null && !fromNumber.isBlank();
  }

  public void sendMessage(String toNumber, String body) {
    if (!isConfigured()) {
      throw new IllegalStateException("WhatsApp client is not configured");
    }

    String to = toNumber.startsWith("whatsapp:") ? toNumber : "whatsapp:" + toNumber;
    String from = fromNumber.startsWith("whatsapp:") ? fromNumber : "whatsapp:" + fromNumber;

    String form = "To=" + url(to)
        + "&From=" + url(from)
        + "&Body=" + url(body);

    String auth = Base64.getEncoder()
        .encodeToString((accountSid + ":" + authToken).getBytes(StandardCharsets.UTF_8));

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://api.twilio.com/2010-04-01/Accounts/" + accountSid + "/Messages.json"))
        .header("Authorization", "Basic " + auth)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .POST(HttpRequest.BodyPublishers.ofString(form))
        .build();

    HttpResponse<String> response;
    try {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("WhatsApp send interrupted", ex);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to reach WhatsApp provider", ex);
    }

    if (response.statusCode() < 200 || response.statusCode() >= 300) {
      throw new IllegalStateException("WhatsApp provider error: " + response.body());
    }
  }

  private String url(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8);
  }
}
