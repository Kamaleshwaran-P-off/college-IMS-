package com.smartcampus.platform.staff.service;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.staff.dto.AssignClassRequest;
import com.smartcampus.platform.staff.dto.FacultyClassSummary;
import com.smartcampus.platform.staff.entity.FacultyClassMapping;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.staff.repository.FacultyClassMappingRepository;
import com.smartcampus.platform.staff.repository.StaffRepository;

@Service
@Transactional
public class FacultyClassAssignmentService {
  private final UserRepository userRepository;
  private final StaffRepository staffRepository;
  private final FacultyClassMappingRepository mappingRepository;

  public FacultyClassAssignmentService(
      UserRepository userRepository,
      StaffRepository staffRepository,
      FacultyClassMappingRepository mappingRepository
  ) {
    this.userRepository = userRepository;
    this.staffRepository = staffRepository;
    this.mappingRepository = mappingRepository;
  }

  public List<String> assignClasses(String adminEmail, AssignClassRequest request) {
    var admin = userRepository.findByEmail(adminEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    if (admin.getRole() != Role.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
    }

    Staff staff = staffRepository.findById(request.getFacultyId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not found"));

    List<String> normalized = request.getClasses().stream()
        .map(this::normalizeClass)
        .filter(value -> !value.isEmpty())
        .distinct()
        .toList();

    if (normalized.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one class is required");
    }

    mappingRepository.deleteByStaffId(staff.getId());
    List<FacultyClassMapping> mappings = normalized.stream()
        .map(className -> {
          ClassParts parts = splitClass(className);
          return new FacultyClassMapping(staff, className, parts.department, parts.section);
        })
        .collect(Collectors.toList());
    mappingRepository.saveAll(mappings);

    staff.setAssignedClasses(String.join(", ", normalized));
    staffRepository.save(staff);

    return normalized;
  }

  public List<String> getAssignedClasses(String facultyEmail) {
    Staff staff = requireStaffUser(facultyEmail);

    List<String> mapped = mappingRepository.findByStaffId(staff.getId()).stream()
        .map(FacultyClassMapping::getClassName)
        .toList();
    if (!mapped.isEmpty()) {
      return mapped;
    }
    if (staff.getAssignedClasses() == null || staff.getAssignedClasses().isBlank()) {
      return List.of();
    }
    return java.util.Arrays.stream(staff.getAssignedClasses().split(","))
        .map(this::normalizeClass)
        .filter(value -> !value.isEmpty())
        .toList();
  }

  public List<FacultyClassSummary> getAssignedClassDetails(String facultyEmail) {
    Staff staff = requireStaffUser(facultyEmail);

    List<FacultyClassMapping> mappings = mappingRepository.findByStaffId(staff.getId());
    if (!mappings.isEmpty()) {
      return mappings.stream()
          .map(mapping -> new FacultyClassSummary(
              mapping.getId(),
              mapping.getClassName(),
              mapping.getDepartment(),
              mapping.getSection()
          ))
          .toList();
    }

    // Fallback for legacy assignedClasses string (no mapping id).
    if (staff.getAssignedClasses() == null || staff.getAssignedClasses().isBlank()) {
      return List.of();
    }
    return java.util.Arrays.stream(staff.getAssignedClasses().split(","))
        .map(this::normalizeClass)
        .filter(value -> !value.isEmpty())
        .map(value -> new FacultyClassSummary(null, value, splitClass(value).department(), splitClass(value).section()))
        .toList();
  }

  public FacultyClassSummary getAssignedClass(String facultyEmail, Long classId) {
    Staff staff = requireStaffUser(facultyEmail);
    FacultyClassMapping mapping = mappingRepository.findById(classId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Class not found"));

    if (!mapping.getStaff().getId().equals(staff.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not assigned to this class");
    }

    return new FacultyClassSummary(
        mapping.getId(),
        mapping.getClassName(),
        mapping.getDepartment(),
        mapping.getSection()
    );
  }

  public boolean hasClassAccess(Long staffId, String department, String section) {
    if (staffId == null) {
      return false;
    }
    String normalizedDepartment = normalize(department);
    String normalizedSection = normalize(section);
    if (normalizedDepartment == null || normalizedSection == null) {
      return false;
    }
    return mappingRepository.existsByStaffIdAndDepartmentIgnoreCaseAndSectionIgnoreCase(
        staffId,
        normalizedDepartment,
        normalizedSection
    );
  }

  public boolean hasClassAccess(Long staffId, String className) {
    if (staffId == null) {
      return false;
    }
    String normalized = normalizeClass(className);
    if (normalized == null || normalized.isBlank()) {
      return false;
    }
    return mappingRepository.existsByStaffIdAndClassNameIgnoreCase(staffId, normalized);
  }

  private String normalizeClass(String value) {
    if (value == null) {
      return "";
    }
    String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      return "";
    }
    return trimmed.toUpperCase(Locale.ROOT);
  }

  private String normalize(String input) {
    if (input == null) {
      return null;
    }
    String trimmed = input.trim();
    if (trimmed.isEmpty()) {
      return null;
    }
    return trimmed.toLowerCase(Locale.ROOT);
  }

  private ClassParts splitClass(String className) {
    if (className == null || className.isBlank()) {
      return new ClassParts(null, null);
    }
    String trimmed = className.trim();
    int dashIndex = trimmed.lastIndexOf("-");
    if (dashIndex > 0 && dashIndex < trimmed.length() - 1) {
      String dept = trimmed.substring(0, dashIndex).trim();
      String section = trimmed.substring(dashIndex + 1).trim();
      return new ClassParts(normalizeClass(dept), normalizeClass(section));
    }
    return new ClassParts(normalizeClass(trimmed), null);
  }

  private record ClassParts(String department, String section) {}

  private Staff requireStaffUser(String email) {
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }

    return staffRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Faculty not found"));
  }
}
