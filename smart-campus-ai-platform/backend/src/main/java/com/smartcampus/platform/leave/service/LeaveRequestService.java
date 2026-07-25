package com.smartcampus.platform.leave.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.common.exception.ResourceNotFoundException;
import com.smartcampus.platform.leave.dto.LeaveCreateRequest;
import com.smartcampus.platform.leave.dto.LeaveDecisionRequest;
import com.smartcampus.platform.leave.dto.LeaveResponse;
import com.smartcampus.platform.leave.entity.LeaveRequest;
import com.smartcampus.platform.leave.entity.LeaveStatus;
import com.smartcampus.platform.leave.repository.LeaveRequestRepository;
import com.smartcampus.platform.mentor.entity.MentorAssignment;
import com.smartcampus.platform.mentor.repository.MentorAssignmentRepository;
import com.smartcampus.platform.notification.entity.NotificationType;
import com.smartcampus.platform.notification.service.NotificationService;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.staff.repository.StaffRepository;
import com.smartcampus.platform.student.entity.Student;
import com.smartcampus.platform.student.repository.StudentRepository;

@Service
@Transactional
public class LeaveRequestService {
  private final LeaveRequestRepository leaveRequestRepository;
  private final StudentRepository studentRepository;
  private final StaffRepository staffRepository;
  private final UserRepository userRepository;
  private final MentorAssignmentRepository mentorAssignmentRepository;
  private final NotificationService notificationService;

  public LeaveRequestService(
      LeaveRequestRepository leaveRequestRepository,
      StudentRepository studentRepository,
      StaffRepository staffRepository,
      UserRepository userRepository,
      MentorAssignmentRepository mentorAssignmentRepository,
      NotificationService notificationService
  ) {
    this.leaveRequestRepository = leaveRequestRepository;
    this.studentRepository = studentRepository;
    this.staffRepository = staffRepository;
    this.userRepository = userRepository;
    this.mentorAssignmentRepository = mentorAssignmentRepository;
    this.notificationService = notificationService;
  }

  public LeaveResponse create(LeaveCreateRequest request) {
    try {
      return createInternal(request, null, null);
    } catch (java.io.IOException ex) {
      throw new IllegalStateException("Failed to store leave request files", ex);
    }
  }

  public LeaveResponse createWithFiles(LeaveCreateRequest request, MultipartFile proofFile, MultipartFile letterFile)
      throws java.io.IOException {
    return createInternal(request, proofFile, letterFile);
  }

  private LeaveResponse createInternal(LeaveCreateRequest request, MultipartFile proofFile, MultipartFile letterFile)
      throws java.io.IOException {
    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    if (user.getRole() != Role.STUDENT) {
      throw new IllegalArgumentException("Only STUDENT can request leave");
    }

    Student student = studentRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

    LocalDate start = request.getStartDate();
    LocalDate end = request.getEndDate();
    if (end != null && end.isBefore(start)) {
      throw new IllegalArgumentException("End date cannot be before start date");
    }

    if (request.getType() == com.smartcampus.platform.leave.entity.LeaveType.RECAT
        || request.getType() == com.smartcampus.platform.leave.entity.LeaveType.OD) {
      if (isEmpty(proofFile) || isEmpty(letterFile)) {
        throw new IllegalArgumentException("Proof and application letter are required for ReCAT/OD");
      }
    }

    MentorAssignment assignment = mentorAssignmentRepository.findByStudentId(student.getId())
        .orElse(null);
    Staff mentor = assignment != null ? assignment.getMentor() : null;

    LeaveRequest leave = new LeaveRequest(
        student,
        mentor,
        request.getType(),
        start,
        end,
        request.getReason(),
        LeaveStatus.PENDING
    );

    if (!isEmpty(proofFile)) {
      leave.setProofFileName(proofFile.getOriginalFilename());
      leave.setProofContentType(proofFile.getContentType());
      leave.setProofFileData(proofFile.getBytes());
    }

    if (!isEmpty(letterFile)) {
      leave.setLetterFileName(letterFile.getOriginalFilename());
      leave.setLetterContentType(letterFile.getContentType());
      leave.setLetterFileData(letterFile.getBytes());
    }

    LeaveRequest saved = leaveRequestRepository.save(leave);

    if (mentor != null) {
      String message = "Leave approval needed for " + student.getUser().getFullName()
          + " (" + student.getStudentCode() + ") - " + request.getType()
          + " starting " + start;
      notificationService.createNotification(
          mentor.getUser().getId(),
          NotificationType.LEAVE,
          "Leave approval pending",
          message
      );
    }

    return toResponse(saved);
  }

  public List<LeaveResponse> findAll(Long studentId, Long mentorId, LeaveStatus status, com.smartcampus.platform.leave.entity.LeaveType type) {
    List<LeaveRequest> results;
    if (studentId != null) {
      results = leaveRequestRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
    } else if (mentorId != null && status != null) {
      results = leaveRequestRepository.findByMentorIdAndStatusOrderByCreatedAtDesc(mentorId, status);
    } else if (mentorId != null) {
      results = leaveRequestRepository.findByMentorIdOrderByCreatedAtDesc(mentorId);
    } else if (status != null) {
      results = leaveRequestRepository.findByStatusOrderByCreatedAtDesc(status);
    } else {
      results = leaveRequestRepository.findAllByOrderByCreatedAtDesc();
    }

    if (type != null) {
      results = results.stream().filter(item -> item.getType() == type).toList();
    }

    return results.stream().map(this::toResponse).toList();
  }

  public LeaveResponse approve(Long id, LeaveDecisionRequest request) {
    return decide(id, request, LeaveStatus.APPROVED);
  }

  public LeaveResponse reject(Long id, LeaveDecisionRequest request) {
    return decide(id, request, LeaveStatus.REJECTED);
  }

  private LeaveResponse decide(Long id, LeaveDecisionRequest request, LeaveStatus status) {
    LeaveRequest leave = leaveRequestRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

    if (leave.getStatus() != LeaveStatus.PENDING) {
      throw new IllegalArgumentException("Leave request already processed");
    }

    User approver = userRepository.findById(request.getApproverUserId())
        .orElseThrow(() -> new ResourceNotFoundException("Approver not found"));

    if (leave.getType() == com.smartcampus.platform.leave.entity.LeaveType.RECAT) {
      if (approver.getRole() != Role.STAFF) {
        throw new IllegalArgumentException("Only assigned faculty can review ReCAT requests");
      }
      Staff staff = staffRepository.findByUserId(approver.getId())
          .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found"));
      if (leave.getMentor() == null || !leave.getMentor().getId().equals(staff.getId())) {
        throw new IllegalArgumentException("Only assigned mentor can review this request");
      }

      if (status == LeaveStatus.APPROVED) {
        leave.setStatus(LeaveStatus.FACULTY_APPROVED);
      } else {
        leave.setStatus(LeaveStatus.REJECTED);
      }
    } else {
      if (approver.getRole() == Role.STAFF) {
        Staff staff = staffRepository.findByUserId(approver.getId())
            .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found"));
        if (leave.getMentor() == null || !leave.getMentor().getId().equals(staff.getId())) {
          throw new IllegalArgumentException("Only assigned mentor can approve this request");
        }
      } else if (approver.getRole() != Role.ADMIN) {
        throw new IllegalArgumentException("Only STAFF or ADMIN can approve leave");
      }
      leave.setStatus(status);
    }

    leave.setDecidedBy(approver);
    leave.setDecidedAt(LocalDateTime.now());
    leave.setDecisionNote(request.getNote());

    LeaveRequest saved = leaveRequestRepository.save(leave);

    if (leave.getType() != com.smartcampus.platform.leave.entity.LeaveType.RECAT) {
      String title = status == LeaveStatus.APPROVED ? "Leave approved" : "Leave rejected";
      String message = "Your " + leave.getType() + " request from " + leave.getStartDate()
          + (leave.getEndDate() != null ? " to " + leave.getEndDate() : "")
          + " was " + status.name().toLowerCase() + ".";
      if (request.getNote() != null && !request.getNote().isBlank()) {
        message = message + " Note: " + request.getNote();
      }
      notificationService.createNotification(
          leave.getStudent().getUser().getId(),
          NotificationType.LEAVE,
          title,
          message
      );
    }

    return toResponse(saved);
  }

  public LeaveResponse adminReviewRecat(Long id, LeaveStatus status, String adminRemarks, String adminEmail) {
    User approver = userRepository.findByEmail(adminEmail)
        .orElseThrow(() -> new ResourceNotFoundException("Admin not found"));
    if (approver.getRole() != Role.ADMIN) {
      throw new IllegalArgumentException("Admin access required");
    }

    LeaveRequest leave = leaveRequestRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));

    if (leave.getType() != com.smartcampus.platform.leave.entity.LeaveType.RECAT) {
      throw new IllegalArgumentException("Only ReCAT applications require admin review");
    }

    if (leave.getStatus() != LeaveStatus.FACULTY_APPROVED) {
      throw new IllegalArgumentException("Only faculty-approved ReCAT applications can be reviewed");
    }

    if (status != LeaveStatus.ADMIN_APPROVED && status != LeaveStatus.ADMIN_REJECTED) {
      throw new IllegalArgumentException("Invalid admin status");
    }

    if (status == LeaveStatus.ADMIN_REJECTED && (adminRemarks == null || adminRemarks.isBlank())) {
      throw new IllegalArgumentException("Admin rejection reason is required");
    }

    leave.setStatus(status);
    leave.setDecidedBy(approver);
    leave.setDecidedAt(LocalDateTime.now());
    leave.setAdminRemarks(adminRemarks);

    LeaveRequest saved = leaveRequestRepository.save(leave);

    String studentMessage;
    String facultyMessage;
    if (status == LeaveStatus.ADMIN_APPROVED) {
      studentMessage = "Your ReCAT application has been approved.";
      facultyMessage = "ReCAT application for " + leave.getStudent().getUser().getFullName() + " has been approved by Admin.";
      notificationService.createNotification(
          leave.getStudent().getUser().getId(),
          NotificationType.SUCCESS,
          "ReCAT approved",
          studentMessage
      );
      if (leave.getMentor() != null) {
        notificationService.createNotification(
            leave.getMentor().getUser().getId(),
            NotificationType.SUCCESS,
            "ReCAT approved",
            facultyMessage
        );
      }
    } else {
      String reason = adminRemarks != null ? adminRemarks : "No reason provided";
      studentMessage = "Your ReCAT application was rejected: " + reason;
      facultyMessage = "ReCAT application for " + leave.getStudent().getUser().getFullName()
          + " was rejected by Admin: " + reason;
      notificationService.createNotification(
          leave.getStudent().getUser().getId(),
          NotificationType.ERROR,
          "ReCAT rejected",
          studentMessage
      );
      if (leave.getMentor() != null) {
        notificationService.createNotification(
            leave.getMentor().getUser().getId(),
            NotificationType.ERROR,
            "ReCAT rejected",
            facultyMessage
        );
      }
    }

    return toResponse(saved);
  }

  private LeaveResponse toResponse(LeaveRequest leave) {
    Long mentorId = leave.getMentor() != null ? leave.getMentor().getId() : null;
    String mentorName = leave.getMentor() != null ? leave.getMentor().getUser().getFullName() : null;
    Long decidedById = leave.getDecidedBy() != null ? leave.getDecidedBy().getId() : null;
    String decidedByName = leave.getDecidedBy() != null ? leave.getDecidedBy().getFullName() : null;

    return new LeaveResponse(
        leave.getId(),
        leave.getStudent().getId(),
        leave.getStudent().getUser().getFullName(),
        leave.getStudent().getStudentCode(),
        mentorId,
        mentorName,
        leave.getType(),
        leave.getStartDate(),
        leave.getEndDate(),
        leave.getReason(),
        leave.getStatus(),
        leave.getCreatedAt(),
        decidedById,
        decidedByName,
        leave.getDecidedAt(),
        leave.getDecisionNote(),
        leave.getAdminRemarks(),
        leave.getProofFileData() != null,
        leave.getLetterFileData() != null
    );
  }

  public LeaveRequest getProof(Long id, String requesterEmail) {
    LeaveRequest leave = requireAccess(id, requesterEmail);
    return leave;
  }

  public LeaveRequest getLetter(Long id, String requesterEmail) {
    LeaveRequest leave = requireAccess(id, requesterEmail);
    return leave;
  }

  private LeaveRequest requireAccess(Long id, String requesterEmail) {
    LeaveRequest leave = leaveRequestRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Leave request not found"));
    User user = userRepository.findByEmail(requesterEmail)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (user.getRole() == Role.ADMIN) {
      return leave;
    }

    if (user.getRole() == Role.STAFF) {
      Staff staff = staffRepository.findByUserId(user.getId())
          .orElseThrow(() -> new ResourceNotFoundException("Staff profile not found"));
      if (leave.getMentor() == null || !Objects.equals(leave.getMentor().getId(), staff.getId())) {
        throw new IllegalArgumentException("Access denied");
      }
      return leave;
    }

    if (user.getRole() == Role.STUDENT) {
      if (!Objects.equals(leave.getStudent().getUser().getId(), user.getId())) {
        throw new IllegalArgumentException("Access denied");
      }
      return leave;
    }

    throw new IllegalArgumentException("Access denied");
  }

  private boolean isEmpty(MultipartFile file) {
    return file == null || file.isEmpty();
  }
}
