package com.smartcampus.platform.mentor.service;

import java.time.LocalDateTime;
import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.common.exception.ResourceNotFoundException;
import com.smartcampus.platform.mentor.dto.MentorAssignRequest;
import com.smartcampus.platform.mentor.dto.MentorAnalyticsResponse;
import com.smartcampus.platform.mentor.dto.MentorAssignmentResponse;
import com.smartcampus.platform.mentor.dto.MentorAutoAllocateRequest;
import com.smartcampus.platform.mentor.entity.MentorAssignment;
import com.smartcampus.platform.mentor.repository.MentorAssignmentRepository;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.staff.repository.StaffRepository;
import com.smartcampus.platform.student.entity.Student;
import com.smartcampus.platform.student.repository.StudentRepository;
import com.smartcampus.platform.leave.entity.LeaveRequest;
import com.smartcampus.platform.leave.entity.LeaveStatus;
import com.smartcampus.platform.leave.repository.LeaveRequestRepository;

@Service
@Transactional
public class MentorAssignmentService {
  private final MentorAssignmentRepository mentorAssignmentRepository;
  private final StudentRepository studentRepository;
  private final StaffRepository staffRepository;
  private final UserRepository userRepository;
  private final LeaveRequestRepository leaveRequestRepository;

  public MentorAssignmentService(
      MentorAssignmentRepository mentorAssignmentRepository,
      StudentRepository studentRepository,
      StaffRepository staffRepository,
      UserRepository userRepository,
      LeaveRequestRepository leaveRequestRepository
  ) {
    this.mentorAssignmentRepository = mentorAssignmentRepository;
    this.studentRepository = studentRepository;
    this.staffRepository = staffRepository;
    this.userRepository = userRepository;
    this.leaveRequestRepository = leaveRequestRepository;
  }

  public MentorAssignmentResponse assignMentor(MentorAssignRequest request) {
    Student student = studentRepository.findById(request.getStudentId())
        .orElseThrow(() -> new ResourceNotFoundException("Student not found"));
    Staff mentor = staffRepository.findById(request.getMentorId())
        .orElseThrow(() -> new ResourceNotFoundException("Staff mentor not found"));

    if (mentor.getUser().getRole() != Role.STAFF) {
      throw new IllegalArgumentException("Selected mentor must have STAFF role");
    }

    User assignedBy = resolveAdminUser(request.getAssignedByUserId());

    MentorAssignment assignment = mentorAssignmentRepository.findByStudentId(student.getId())
        .orElseGet(MentorAssignment::new);
    assignment.setStudent(student);
    assignment.setMentor(mentor);
    assignment.setAssignedBy(assignedBy);
    assignment.setAssignedAt(LocalDateTime.now());

    return toResponse(mentorAssignmentRepository.save(assignment));
  }

  public List<MentorAssignmentResponse> autoAllocate(MentorAutoAllocateRequest request) {
    User assignedBy = resolveAdminUser(request != null ? request.getAssignedByUserId() : null);

    List<Student> students = studentRepository.findAll();
    List<Staff> mentors = staffRepository.findAll().stream()
        .filter(staff -> staff.getUser().getRole() == Role.STAFF)
        .toList();
    if (mentors.isEmpty()) {
      throw new IllegalStateException("No staff mentors available");
    }

    List<MentorAssignment> existing = mentorAssignmentRepository.findAll();
    Set<Long> assignedStudentIds = new HashSet<>();
    Map<Long, Long> mentorLoad = new HashMap<>();
    for (MentorAssignment assignment : existing) {
      assignedStudentIds.add(assignment.getStudent().getId());
      mentorLoad.merge(assignment.getMentor().getId(), 1L, Long::sum);
    }

    List<MentorAssignmentResponse> created = new ArrayList<>();
    for (Student student : students) {
      if (assignedStudentIds.contains(student.getId())) {
        continue;
      }

      Staff chosen = chooseMentor(student, mentors, mentorLoad);
      MentorAssignment assignment = new MentorAssignment(student, chosen, assignedBy);
      assignment.setAssignedAt(LocalDateTime.now());
      MentorAssignment saved = mentorAssignmentRepository.save(assignment);
      mentorLoad.merge(chosen.getId(), 1L, Long::sum);
      created.add(toResponse(saved));
    }

    return created;
  }

  public List<MentorAssignmentResponse> findAll(Long studentId, Long mentorId) {
    List<MentorAssignment> assignments;
    if (studentId != null) {
      assignments = mentorAssignmentRepository.findByStudentId(studentId)
          .map(List::of)
          .orElseGet(List::of);
    } else if (mentorId != null) {
      assignments = mentorAssignmentRepository.findByMentorId(mentorId);
    } else {
      assignments = mentorAssignmentRepository.findAll();
    }

    return assignments.stream().map(this::toResponse).toList();
  }

  public List<MentorAnalyticsResponse> getAnalytics(Long adminUserId) {
    resolveAdminUser(adminUserId);

    List<Staff> mentors = staffRepository.findAll().stream()
        .filter(staff -> staff.getUser().getRole() == Role.STAFF)
        .toList();

    return mentors.stream().map(mentor -> {
      long menteeCount = mentorAssignmentRepository.countByMentorId(mentor.getId());
      List<LeaveRequest> decisions = leaveRequestRepository.findByMentorIdOrderByCreatedAtDesc(mentor.getId())
          .stream()
          .filter(leave -> leave.getDecidedAt() != null
              && (leave.getStatus() == LeaveStatus.APPROVED || leave.getStatus() == LeaveStatus.REJECTED))
          .toList();

      long totalDecisions = decisions.size();
      Double avgHours = null;
      if (totalDecisions > 0) {
        double totalMinutes = 0;
        for (LeaveRequest leave : decisions) {
          totalMinutes += java.time.Duration.between(leave.getCreatedAt(), leave.getDecidedAt()).toMinutes();
        }
        avgHours = totalMinutes / 60.0 / totalDecisions;
      }

      return new MentorAnalyticsResponse(
          mentor.getId(),
          mentor.getUser().getFullName(),
          mentor.getStaffCode(),
          mentor.getDepartment(),
          menteeCount,
          totalDecisions,
          avgHours
      );
    }).toList();
  }

  private Staff chooseMentor(Student student, List<Staff> mentors, Map<Long, Long> load) {
    String dept = student.getDepartment();
    List<Staff> candidates = new ArrayList<>();
    if (dept != null && !dept.isBlank()) {
      for (Staff mentor : mentors) {
        if (mentor.getDepartment() != null && dept.equalsIgnoreCase(mentor.getDepartment())) {
          candidates.add(mentor);
        }
      }
    }
    if (candidates.isEmpty()) {
      candidates = mentors;
    }

    Staff best = candidates.get(0);
    long bestLoad = load.getOrDefault(best.getId(), 0L);
    for (Staff candidate : candidates) {
      long candidateLoad = load.getOrDefault(candidate.getId(), 0L);
      if (candidateLoad < bestLoad) {
        best = candidate;
        bestLoad = candidateLoad;
      }
    }
    return best;
  }

  private User resolveAdminUser(Long userId) {
    if (userId == null) {
      throw new IllegalArgumentException("Admin user is required for mentor allocation");
    }
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    if (user.getRole() != Role.ADMIN) {
      throw new IllegalArgumentException("Only ADMIN can assign mentors");
    }
    return user;
  }

  private MentorAssignmentResponse toResponse(MentorAssignment assignment) {
    return new MentorAssignmentResponse(
        assignment.getId(),
        assignment.getStudent().getId(),
        assignment.getStudent().getUser().getFullName(),
        assignment.getStudent().getStudentCode(),
        assignment.getMentor().getId(),
        assignment.getMentor().getUser().getFullName(),
        assignment.getMentor().getStaffCode(),
        assignment.getMentor().getDepartment(),
        assignment.getAssignedAt()
    );
  }
}
