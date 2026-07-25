package com.smartcampus.platform.staff.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.common.exception.ResourceNotFoundException;
import com.smartcampus.platform.staff.dto.StaffRequest;
import com.smartcampus.platform.staff.dto.StaffResponse;
import com.smartcampus.platform.staff.entity.FacultyClassMapping;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.staff.repository.FacultyClassMappingRepository;
import com.smartcampus.platform.staff.repository.StaffRepository;
import com.smartcampus.platform.staff.service.StaffProfileService;

@Service
@Transactional
public class StaffService {
  private final StaffRepository staffRepository;
  private final UserRepository userRepository;
  private final StaffProfileService staffProfileService;
  private final FacultyClassMappingRepository mappingRepository;

  public StaffService(
      StaffRepository staffRepository,
      UserRepository userRepository,
      StaffProfileService staffProfileService,
      FacultyClassMappingRepository mappingRepository
  ) {
    this.staffRepository = staffRepository;
    this.userRepository = userRepository;
    this.staffProfileService = staffProfileService;
    this.mappingRepository = mappingRepository;
  }

  public StaffResponse create(StaffRequest request) {
    User user = userRepository.findById(request.getUserId())
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    if (user.getRole() != Role.STAFF && user.getRole() != Role.ADMIN) {
      throw new IllegalArgumentException("User role must be STAFF or ADMIN to create a staff profile");
    }

    if (staffRepository.existsByUserId(user.getId())) {
      throw new IllegalArgumentException("Staff profile already exists for this user");
    }

    Staff staff = new Staff(
        user,
        request.getStaffCode(),
        request.getDepartment(),
        request.getDesignation(),
        request.getPhone(),
        (request.getAssignedClasses() == null || request.getAssignedClasses().isBlank()) ? "CSE-A" : request.getAssignedClasses(),
        request.getSkills(),
        request.getInterests()
    );

    Staff saved = staffRepository.save(staff);
    syncMappings(saved, saved.getAssignedClasses());
    return toResponse(saved);
  }

  public List<StaffResponse> findAll() {
    return staffRepository.findAll().stream().map(this::toResponse).toList();
  }

  public StaffResponse findById(Long id) {
    return toResponse(getStaff(id));
  }

  public StaffResponse findByUserId(Long userId) {
    Staff staff = staffProfileService.getOrCreateByUserId(userId);
    return toResponse(staff);
  }

  public StaffResponse update(Long id, StaffRequest request) {
    Staff staff = getStaff(id);
    if (!staff.getUser().getId().equals(request.getUserId())) {
      User user = userRepository.findById(request.getUserId())
          .orElseThrow(() -> new ResourceNotFoundException("User not found"));
      if (user.getRole() != Role.STAFF && user.getRole() != Role.ADMIN) {
        throw new IllegalArgumentException("User role must be STAFF or ADMIN to create a staff profile");
      }
      if (staffRepository.existsByUserId(user.getId())) {
        throw new IllegalArgumentException("Staff profile already exists for this user");
      }
      staff.setUser(user);
    }

    staff.setStaffCode(request.getStaffCode());
    staff.setDepartment(request.getDepartment());
    staff.setDesignation(request.getDesignation());
    staff.setPhone(request.getPhone());
    String updatedClasses = (request.getAssignedClasses() == null || request.getAssignedClasses().isBlank())
        ? staff.getAssignedClasses()
        : request.getAssignedClasses();
    staff.setAssignedClasses(updatedClasses);
    staff.setSkills(request.getSkills());
    staff.setInterests(request.getInterests());

    Staff saved = staffRepository.save(staff);
    syncMappings(saved, updatedClasses);
    return toResponse(saved);
  }

  public void delete(Long id) {
    Staff staff = getStaff(id);
    staffRepository.delete(staff);
  }

  private Staff getStaff(Long id) {
    return staffRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Staff not found"));
  }

  private StaffResponse toResponse(Staff staff) {
    return new StaffResponse(
        staff.getId(),
        staff.getUser().getId(),
        staff.getUser().getFullName(),
        staff.getUser().getEmail(),
        staff.getStaffCode(),
        staff.getDepartment(),
        staff.getDesignation(),
        staff.getPhone(),
        staff.getAssignedClasses(),
        staff.getSkills(),
        staff.getInterests()
    );
  }

  private void syncMappings(Staff staff, String assignedClasses) {
    if (staff.getId() == null) {
      return;
    }
    List<String> classes = splitClasses(assignedClasses);
    if (classes.isEmpty()) {
      return;
    }
    mappingRepository.deleteByStaffId(staff.getId());
    List<FacultyClassMapping> mappings = new ArrayList<>();
    for (String className : classes) {
      ClassParts parts = splitClass(className);
      mappings.add(new FacultyClassMapping(staff, className, parts.department, parts.section));
    }
    mappingRepository.saveAll(mappings);
  }

  private List<String> splitClasses(String raw) {
    if (raw == null || raw.isBlank()) {
      return List.of();
    }
    String[] parts = raw.split(",");
    List<String> results = new ArrayList<>();
    for (String part : parts) {
      String normalized = normalize(part);
      if (normalized != null) {
        results.add(normalized);
      }
    }
    return results;
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

  private ClassParts splitClass(String className) {
    if (className == null || className.isBlank()) {
      return new ClassParts(null, null);
    }
    String trimmed = className.trim();
    int dashIndex = trimmed.lastIndexOf("-");
    if (dashIndex > 0 && dashIndex < trimmed.length() - 1) {
      String dept = trimmed.substring(0, dashIndex).trim();
      String section = trimmed.substring(dashIndex + 1).trim();
      return new ClassParts(normalize(dept), normalize(section));
    }
    return new ClassParts(normalize(trimmed), null);
  }

  private record ClassParts(String department, String section) {}
}
