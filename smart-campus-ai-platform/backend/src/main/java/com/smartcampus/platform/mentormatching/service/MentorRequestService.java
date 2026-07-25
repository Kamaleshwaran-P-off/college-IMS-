package com.smartcampus.platform.mentormatching.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.mentormatching.dto.MentorRequestCreateRequest;
import com.smartcampus.platform.mentormatching.dto.MentorRequestResponse;
import com.smartcampus.platform.mentormatching.dto.MentorRequestStatusRequest;
import com.smartcampus.platform.mentormatching.entity.MentorRequest;
import com.smartcampus.platform.mentormatching.entity.MentorRequestStatus;
import com.smartcampus.platform.mentormatching.repository.MentorRequestRepository;
import com.smartcampus.platform.notification.entity.NotificationType;
import com.smartcampus.platform.notification.service.NotificationService;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.staff.repository.StaffRepository;
import com.smartcampus.platform.student.entity.Student;
import com.smartcampus.platform.student.service.StudentProfileService;

@Service
@Transactional
public class MentorRequestService {
  private final MentorRequestRepository requestRepository;
  private final UserRepository userRepository;
  private final StaffRepository staffRepository;
  private final StudentProfileService studentProfileService;
  private final NotificationService notificationService;

  public MentorRequestService(
      MentorRequestRepository requestRepository,
      UserRepository userRepository,
      StaffRepository staffRepository,
      StudentProfileService studentProfileService,
      NotificationService notificationService
  ) {
    this.requestRepository = requestRepository;
    this.userRepository = userRepository;
    this.staffRepository = staffRepository;
    this.studentProfileService = studentProfileService;
    this.notificationService = notificationService;
  }

  public MentorRequestResponse createRequest(String studentEmail, MentorRequestCreateRequest request) {
    var user = userRepository.findByEmail(studentEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STUDENT) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
    }

    Student student = studentProfileService.ensureForUser(user);
    Staff mentor = staffRepository.findById(request.getMentorId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Mentor not found"));

    MentorRequest mentorRequest = new MentorRequest(
        student,
        mentor,
        MentorRequestStatus.PENDING,
        request.getMessage(),
        LocalDateTime.now()
    );
    MentorRequest saved = requestRepository.save(mentorRequest);
    notificationService.createNotification(
        mentor.getUser().getId(),
        NotificationType.INFO,
        "New mentor request",
        student.getUser().getFullName() + " requested mentorship."
    );
    notificationService.createNotification(
        student.getUser().getId(),
        NotificationType.SUCCESS,
        "Mentor request sent",
        "Your mentor request has been sent to " + mentor.getUser().getFullName() + "."
    );
    return toResponse(saved);
  }

  public MentorRequestResponse updateStatus(String facultyEmail, MentorRequestStatusRequest request) {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }
    Staff staff = staffRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not found"));

    MentorRequest mentorRequest = requestRepository.findById(request.getRequestId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Request not found"));
    if (!mentorRequest.getMentor().getId().equals(staff.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can update only your requests");
    }

    mentorRequest.setStatus(request.getStatus());
    mentorRequest.setRespondedAt(LocalDateTime.now());
    MentorRequest saved = requestRepository.save(mentorRequest);

    if (saved.getStatus() == MentorRequestStatus.ACCEPTED) {
      notificationService.createNotification(
          saved.getStudent().getUser().getId(),
          NotificationType.SUCCESS,
          "Mentor request accepted",
          staff.getUser().getFullName() + " accepted your mentorship request."
      );
    } else if (saved.getStatus() == MentorRequestStatus.REJECTED) {
      notificationService.createNotification(
          saved.getStudent().getUser().getId(),
          NotificationType.ERROR,
          "Mentor request rejected",
          staff.getUser().getFullName() + " rejected your mentorship request."
      );
    }

    return toResponse(saved);
  }

  public List<MentorRequestResponse> listForUser(String email) {
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    if (user.getRole() == Role.STUDENT) {
      Student student = studentProfileService.ensureForUser(user);
      return requestRepository.findByStudentIdOrderByRequestedAtDesc(student.getId())
          .stream()
          .map(this::toResponse)
          .toList();
    }

    if (user.getRole() == Role.STAFF) {
      Staff staff = staffRepository.findByUserId(user.getId())
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not found"));
      return requestRepository.findByMentorIdOrderByRequestedAtDesc(staff.getId())
          .stream()
          .map(this::toResponse)
          .toList();
    }

    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
  }

  private MentorRequestResponse toResponse(MentorRequest request) {
    return new MentorRequestResponse(
        request.getId(),
        request.getStudent().getId(),
        request.getStudent().getUser().getFullName(),
        request.getMentor().getId(),
        request.getMentor().getUser().getFullName(),
        request.getStatus(),
        request.getMessage(),
        request.getRequestedAt() != null ? request.getRequestedAt().toString() : null,
        request.getRespondedAt() != null ? request.getRespondedAt().toString() : null
    );
  }
}
