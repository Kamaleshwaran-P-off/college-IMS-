package com.smartcampus.platform.gmail.controller;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.smartcampus.platform.gmail.service.GmailOAuthService;

@RestController
@RequestMapping("/api/gmail")
public class GmailCallbackController {
  private final GmailOAuthService oauthService;

  public GmailCallbackController(GmailOAuthService oauthService) {
    this.oauthService = oauthService;
  }

  @GetMapping("/oauth2/callback")
  public ResponseEntity<Void> callback(
      @RequestParam("code") String code,
      @RequestParam("state") String state
  ) {
    String redirect = oauthService.handleCallback(code, state);
    HttpHeaders headers = new HttpHeaders();
    headers.setLocation(URI.create(redirect));
    return new ResponseEntity<>(headers, HttpStatus.FOUND);
  }
}
