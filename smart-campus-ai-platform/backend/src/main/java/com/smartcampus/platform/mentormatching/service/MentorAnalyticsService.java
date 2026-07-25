package com.smartcampus.platform.mentormatching.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.mentormatching.dto.MentorAnalyticsResponse;
import com.smartcampus.platform.mentormatching.entity.MentorMatch;
import com.smartcampus.platform.mentormatching.entity.MentorRequest;
import com.smartcampus.platform.mentormatching.entity.MentorRequestStatus;
import com.smartcampus.platform.mentormatching.repository.MentorMatchRepository;
import com.smartcampus.platform.mentormatching.repository.MentorRequestRepository;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.staff.repository.StaffRepository;

@Service
@Transactional(readOnly = true)
public class MentorAnalyticsService {
  private final UserRepository userRepository;
  private final StaffRepository staffRepository;
  private final MentorMatchRepository mentorMatchRepository;
  private final MentorRequestRepository mentorRequestRepository;

  public MentorAnalyticsService(
      UserRepository userRepository,
      StaffRepository staffRepository,
      MentorMatchRepository mentorMatchRepository,
      MentorRequestRepository mentorRequestRepository
  ) {
    this.userRepository = userRepository;
    this.staffRepository = staffRepository;
    this.mentorMatchRepository = mentorMatchRepository;
    this.mentorRequestRepository = mentorRequestRepository;
  }

  public List<MentorAnalyticsResponse> getAnalytics(String adminEmail) {
    var user = userRepository.findByEmail(adminEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
    }

    return staffRepository.findAll()
        .stream()
        .filter(staff -> staff.getUser() != null && staff.getUser().getRole() == Role.STAFF)
        .map(this::toResponse)
        .toList();
  }

  private MentorAnalyticsResponse toResponse(Staff staff) {
    List<MentorMatch> matches = mentorMatchRepository.findByMentorId(staff.getId());
    long totalMatches = matches.size();
    Double averageScore = null;
    if (!matches.isEmpty()) {
      double sum = matches.stream()
          .mapToDouble(match -> match.getScore() != null ? match.getScore() : 0)
          .sum();
      averageScore = Math.round((sum / matches.size()) * 100.0) / 100.0;
    }

    List<MentorRequest> requests = mentorRequestRepository.findByMentorIdOrderByRequestedAtDesc(staff.getId());
    long pending = requests.stream().filter(req -> req.getStatus() == MentorRequestStatus.PENDING).count();
    long accepted = requests.stream().filter(req -> req.getStatus() == MentorRequestStatus.ACCEPTED).count();
    long rejected = requests.stream().filter(req -> req.getStatus() == MentorRequestStatus.REJECTED).count();

    return new MentorAnalyticsResponse(
        staff.getId(),
        staff.getUser().getFullName(),
        staff.getDepartment(),
        totalMatches,
        averageScore,
        pending,
        accepted,
        rejected
    );
  }
}
