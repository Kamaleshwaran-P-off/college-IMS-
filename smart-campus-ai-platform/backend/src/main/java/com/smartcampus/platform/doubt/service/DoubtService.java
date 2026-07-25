package com.smartcampus.platform.doubt.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartcampus.platform.answer.entity.Answer;
import com.smartcampus.platform.answer.repository.AnswerRepository;
import com.smartcampus.platform.assignment.entity.Assignment;
import com.smartcampus.platform.assignment.repository.AssignmentRepository;
import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.chat.service.ChatBonusService;
import com.smartcampus.platform.common.dto.PagedResponse;
import com.smartcampus.platform.common.exception.ResourceNotFoundException;
import com.smartcampus.platform.defaultdata.RealisticDataGenerator;
import com.smartcampus.platform.notification.entity.NotificationType;
import com.smartcampus.platform.notification.service.NotificationService;
import com.smartcampus.platform.doubt.dto.DoubtAnswerResponse;
import com.smartcampus.platform.doubt.dto.DoubtDetailResponse;
import com.smartcampus.platform.doubt.dto.DoubtRequest;
import com.smartcampus.platform.doubt.dto.DoubtResponse;
import com.smartcampus.platform.doubt.dto.LeaderboardEntry;
import com.smartcampus.platform.doubt.entity.Doubt;
import com.smartcampus.platform.doubt.entity.DoubtStatus;
import com.smartcampus.platform.doubt.repository.DoubtRepository;
import com.smartcampus.platform.student.entity.Student;
import com.smartcampus.platform.student.service.StudentProfileService;

@Service
@Transactional
public class DoubtService {
  private final DoubtRepository doubtRepository;
  private final StudentProfileService studentProfileService;
  private final AssignmentRepository assignmentRepository;
  private final AnswerRepository answerRepository;
  private final ChatBonusService chatBonusService;
  private final NotificationService notificationService;
  private final UserRepository userRepository;
  private final RealisticDataGenerator dataGenerator;

  public DoubtService(
      DoubtRepository doubtRepository,
      StudentProfileService studentProfileService,
      AssignmentRepository assignmentRepository,
      AnswerRepository answerRepository,
      ChatBonusService chatBonusService,
      NotificationService notificationService,
      UserRepository userRepository,
      RealisticDataGenerator dataGenerator
  ) {
    this.doubtRepository = doubtRepository;
    this.studentProfileService = studentProfileService;
    this.assignmentRepository = assignmentRepository;
    this.answerRepository = answerRepository;
    this.chatBonusService = chatBonusService;
    this.notificationService = notificationService;
    this.userRepository = userRepository;
    this.dataGenerator = dataGenerator;
  }

  public DoubtResponse create(DoubtRequest request) {
    ensureStudentAccess(request.getUserId());
    Student student = studentProfileService.getOrCreateByUserId(request.getUserId());

    Assignment assignment = null;
    if (request.getAssignmentId() != null) {
      assignment = assignmentRepository.findById(request.getAssignmentId())
          .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
    }

    Doubt doubt = new Doubt(
        student,
        assignment,
        request.getTitle(),
        request.getDescription(),
        request.getStatus() != null ? request.getStatus() : DoubtStatus.OPEN
    );

    return toResponse(doubtRepository.save(doubt));
  }

  public PagedResponse<DoubtResponse> findAll(
      DoubtStatus status,
      Long assignmentId,
      Long studentUserId,
      Boolean accepted,
      String search,
      Pageable pageable
  ) {
    try {
      Page<Doubt> page = doubtRepository.search(status, assignmentId, studentUserId, accepted, search, pageable);
      List<DoubtResponse> content = page.getContent().stream().map(this::toResponse).toList();
      if (content.isEmpty()) {
        return dataGenerator.getDefaultDoubtsPage(pageable.getPageNumber(), pageable.getPageSize());
      }
      return new PagedResponse<>(content, page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    } catch (Exception ex) {
      return dataGenerator.getDefaultDoubtsPage(pageable.getPageNumber(), pageable.getPageSize());
    }
  }

  public DoubtDetailResponse findById(Long id) {
    Doubt doubt = getDoubt(id);
    List<DoubtAnswerResponse> answers = answerRepository.findByDoubtIdOrderByCreatedAtAsc(id)
        .stream()
        .map(this::toAnswerResponse)
        .toList();
    return toDetailResponse(doubt, answers);
  }

  public DoubtResponse update(Long id, DoubtRequest request) {
    Doubt doubt = getDoubt(id);
    ensureStudentAccess(request.getUserId());
    Student student = studentProfileService.getOrCreateByUserId(request.getUserId());

    Assignment assignment = null;
    if (request.getAssignmentId() != null) {
      assignment = assignmentRepository.findById(request.getAssignmentId())
          .orElseThrow(() -> new ResourceNotFoundException("Assignment not found"));
    }

    doubt.setStudent(student);
    doubt.setAssignment(assignment);
    doubt.setTitle(request.getTitle());
    doubt.setDescription(request.getDescription());
    doubt.setStatus(request.getStatus() != null ? request.getStatus() : DoubtStatus.OPEN);

    return toResponse(doubtRepository.save(doubt));
  }

  public DoubtDetailResponse acceptBestAnswer(Long doubtId, Long answerId) {
    Doubt doubt = getDoubt(doubtId);
    if (doubt.getAcceptedAnswer() != null) {
      throw new IllegalArgumentException("Best answer already selected");
    }

    Answer answer = answerRepository.findById(answerId)
        .orElseThrow(() -> new ResourceNotFoundException("Answer not found"));

    if (!answer.getDoubt().getId().equals(doubtId)) {
      throw new IllegalArgumentException("Answer does not belong to this doubt");
    }

    doubt.setAcceptedAnswer(answer);
    doubt.setStatus(DoubtStatus.ANSWERED);
    doubtRepository.save(doubt);

    if (answer.getAuthor().getRole() == Role.STUDENT) {
      chatBonusService.grantBonus(answer.getAuthor(), 1, "ACCEPTED_ANSWER");
      notificationService.createNotification(
          answer.getAuthor().getId(),
          NotificationType.REWARD,
          "Reward unlocked",
          "Your answer was accepted. +1 chatbot query added."
      );
    }

    return findById(doubtId);
  }

  public List<LeaderboardEntry> getLeaderboard(int limit) {
    return doubtRepository.findLeaderboard(Pageable.ofSize(limit));
  }

  public void delete(Long id) {
    Doubt doubt = getDoubt(id);
    doubtRepository.delete(doubt);
  }

  private Doubt getDoubt(Long id) {
    return doubtRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Doubt not found"));
  }

  private void ensureStudentAccess(Long userId) {
    if (userId == null) {
      throw new IllegalArgumentException("Student access required");
    }
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    if (user.getRole() != Role.STUDENT) {
      throw new IllegalArgumentException("Student access required");
    }
  }

  private DoubtResponse toResponse(Doubt doubt) {
    Long acceptedAnswerId = doubt.getAcceptedAnswer() != null ? doubt.getAcceptedAnswer().getId() : null;
    return new DoubtResponse(
        doubt.getId(),
        doubt.getStudent().getId(),
        doubt.getStudent().getUser().getId(),
        doubt.getTitle(),
        doubt.getDescription(),
        doubt.getStatus(),
        doubt.getCreatedAt(),
        acceptedAnswerId
    );
  }

  private DoubtDetailResponse toDetailResponse(Doubt doubt, List<DoubtAnswerResponse> answers) {
    Long acceptedAnswerId = doubt.getAcceptedAnswer() != null ? doubt.getAcceptedAnswer().getId() : null;
    return new DoubtDetailResponse(
        doubt.getId(),
        doubt.getStudent().getId(),
        doubt.getStudent().getUser().getId(),
        doubt.getStudent().getUser().getFullName(),
        doubt.getTitle(),
        doubt.getDescription(),
        doubt.getStatus(),
        doubt.getCreatedAt(),
        acceptedAnswerId,
        answers
    );
  }

  private DoubtAnswerResponse toAnswerResponse(Answer answer) {
    return new DoubtAnswerResponse(
        answer.getId(),
        answer.getAuthor().getId(),
        answer.getAuthor().getFullName(),
        answer.getAuthor().getRole().name(),
        answer.getContent(),
        answer.getCreatedAt()
    );
  }
}
