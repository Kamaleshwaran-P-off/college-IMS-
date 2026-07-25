package com.smartcampus.platform.emailintelligence.hackathon;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.emailintelligence.hackathon.dto.EmailHackathonResponse;

@RestController
@RequestMapping("/api/email-intelligence/hackathons")
public class EmailHackathonController {
  private final EmailHackathonService hackathonService;
  private final UserRepository userRepository;

  public EmailHackathonController(EmailHackathonService hackathonService, UserRepository userRepository) {
    this.hackathonService = hackathonService;
    this.userRepository = userRepository;
  }

  @GetMapping
  public List<EmailHackathonResponse> getHackathons(Authentication authentication) {
    Long userId = getUserId(authentication);
    return hackathonService.getHackathons(userId);
  }

  private Long getUserId(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authentication");
    }
    String email = authentication.getName();
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    return user.getId();
  }
}
