package com.smartcampus.platform.staff.service;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.staff.dto.AssignSubjectRequest;
import com.smartcampus.platform.staff.dto.FacultySubjectResponse;
import com.smartcampus.platform.staff.entity.FacultySubjectMapping;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.staff.repository.FacultySubjectMappingRepository;
import com.smartcampus.platform.staff.repository.StaffRepository;

@Service
@Transactional
public class FacultySubjectAssignmentService {
  private final UserRepository userRepository;
  private final StaffRepository staffRepository;
  private final FacultySubjectMappingRepository mappingRepository;

  public FacultySubjectAssignmentService(
      UserRepository userRepository,
      StaffRepository staffRepository,
      FacultySubjectMappingRepository mappingRepository
  ) {
    this.userRepository = userRepository;
    this.staffRepository = staffRepository;
    this.mappingRepository = mappingRepository;
  }

  public FacultySubjectResponse assign(String adminEmail, AssignSubjectRequest request) {
    var admin = userRepository.findByEmail(adminEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    if (admin.getRole() != Role.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
    }

    Staff staff = staffRepository.findById(request.getFacultyId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not found"));

    String subject = normalize(request.getSubject());
    String department = normalize(request.getDepartment());
    String section = normalize(request.getSection());

    mappingRepository.findByStaffIdAndSubjectIgnoreCaseAndDepartmentIgnoreCaseAndSectionIgnoreCase(
        staff.getId(),
        subject,
        department,
        section
    ).ifPresent(existing -> {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Subject already assigned");
    });

    FacultySubjectMapping mapping = new FacultySubjectMapping(staff, subject, department, section);
    FacultySubjectMapping saved = mappingRepository.save(mapping);
    return toResponse(saved);
  }

  public List<FacultySubjectResponse> getForFaculty(String facultyEmail) {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }

    Staff staff = staffRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not found"));

    return mappingRepository.findByStaffId(staff.getId())
        .stream()
        .map(this::toResponse)
        .toList();
  }

  public boolean hasFacultyAccess(Long staffId, String subject, String department, String section) {
    if (staffId == null) {
      return false;
    }
    return mappingRepository.findByStaffIdAndSubjectIgnoreCaseAndDepartmentIgnoreCaseAndSectionIgnoreCase(
        staffId,
        subject,
        department,
        section
    ).isPresent();
  }

  public boolean hasAnyAssignment(Long staffId) {
    if (staffId == null) {
      return false;
    }
    return mappingRepository.existsByStaffId(staffId);
  }

  private FacultySubjectResponse toResponse(FacultySubjectMapping mapping) {
    return new FacultySubjectResponse(
        mapping.getId(),
        mapping.getStaff().getId(),
        mapping.getStaff().getUser().getFullName(),
        mapping.getSubject(),
        mapping.getDepartment(),
        mapping.getSection()
    );
  }

  private String normalize(String value) {
    if (value == null) {
      return null;
    }
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      return null;
    }
    return trimmed.toUpperCase(Locale.ROOT);
  }
}
