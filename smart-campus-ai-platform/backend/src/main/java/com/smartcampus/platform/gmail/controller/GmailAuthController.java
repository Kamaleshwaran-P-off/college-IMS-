package com.smartcampus.platform.gmail.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.auth.security.JwtService;
import com.smartcampus.platform.gmail.dto.GmailAuthUrlResponse;
import com.smartcampus.platform.gmail.dto.GmailStatusResponse;
import com.smartcampus.platform.gmail.repository.GmailOverrideRepository;
import com.smartcampus.platform.gmail.repository.GmailTokenRepository;
import com.smartcampus.platform.gmail.service.GmailOAuthService;

@RestController
@RequestMapping("/api/gmail")
public class GmailAuthController {
  private final GmailOAuthService oauthService;
  private final UserRepository userRepository;
  private final GmailTokenRepository tokenRepository;
  private final GmailOverrideRepository overrideRepository;
  private final JwtService jwtService;

  public GmailAuthController(
      GmailOAuthService oauthService,
      UserRepository userRepository,
      GmailTokenRepository tokenRepository,
      GmailOverrideRepository overrideRepository,
      JwtService jwtService
  ) {
    this.oauthService = oauthService;
    this.userRepository = userRepository;
    this.tokenRepository = tokenRepository;
    this.overrideRepository = overrideRepository;
    this.jwtService = jwtService;
  }

  @GetMapping("/connect")
  public ResponseEntity<Void> connect(
      Authentication authentication,
      @RequestParam(value = "token", required = false) String token
  ) {
    String email = resolveEmail(authentication, token);
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    String url = oauthService.buildAuthUrl(user.getId());
    return ResponseEntity.status(HttpStatus.FOUND)
        .header("Location", url)
        .build();
  }

  @GetMapping("/auth-url")
  public ResponseEntity<GmailAuthUrlResponse> authUrl(Authentication authentication) {
    String email = authentication.getName();
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    String url = oauthService.buildAuthUrl(user.getId());
    return ResponseEntity.ok(new GmailAuthUrlResponse(url));
  }

  @GetMapping("/status")
  public ResponseEntity<GmailStatusResponse> status(Authentication authentication) {
    String email = authentication.getName();
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    boolean linked = tokenRepository.findByUserId(user.getId()).isPresent();
    return ResponseEntity.ok(new GmailStatusResponse(linked));
  }

  @DeleteMapping("/disconnect")
  public ResponseEntity<Void> disconnect(Authentication authentication) {
    String email = authentication.getName();
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    overrideRepository.findByUserId(user.getId())
        .forEach(overrideRepository::delete);
    tokenRepository.findByUserId(user.getId())
        .ifPresent(tokenRepository::delete);

    return ResponseEntity.noContent().build();
  }

  private String resolveEmail(Authentication authentication, String token) {
    if (authentication != null && authentication.isAuthenticated()) {
      return authentication.getName();
    }
    if (token != null && !token.isBlank()) {
      return jwtService.extractUsername(token);
    }
    throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authentication");
  }
}
