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
import com.smartcampus.platform.staff.entity.FacultyClassMapping;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.staff.repository.FacultyClassMappingRepository;
import com.smartcampus.platform.staff.repository.StaffRepository;

@Service
@Transactional
public class StaffProfileService {
  private final StaffRepository staffRepository;
  private final UserRepository userRepository;
  private final FacultyClassMappingRepository mappingRepository;

  public StaffProfileService(
      StaffRepository staffRepository,
      UserRepository userRepository,
      FacultyClassMappingRepository mappingRepository
  ) {
    this.staffRepository = staffRepository;
    this.userRepository = userRepository;
    this.mappingRepository = mappingRepository;
  }

  public Staff getOrCreateByUserId(Long userId) {
    Staff staff = staffRepository.findByUserId(userId)
        .orElseGet(() -> createDefaultProfile(loadUser(userId)));
    return ensureDefaults(staff);
  }

  public Staff ensureForUser(User user) {
    if (user == null) {
      throw new ResourceNotFoundException("User not found");
    }
    Staff staff = staffRepository.findByUserId(user.getId())
        .orElseGet(() -> createDefaultProfile(user));
    return ensureDefaults(staff);
  }

  private User loadUser(Long userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));
  }

  private Staff createDefaultProfile(User user) {
    if (user.getRole() != Role.STAFF) {
      throw new IllegalArgumentException("Faculty access required");
    }
    String staffCode = "AUTO-STAFF-" + user.getId();
    Staff staff = new Staff(user, staffCode, null, null, null, "CSE-A", null, null);
    return staffRepository.save(staff);
  }

  private Staff ensureDefaults(Staff staff) {
    boolean updated = false;
    if (staff.getAssignedClasses() == null || staff.getAssignedClasses().isBlank()) {
      staff.setAssignedClasses("CSE-A");
      updated = true;
    }

    if (updated) {
      staff = staffRepository.save(staff);
    }

    ensureMappings(staff);
    return staff;
  }

  private void ensureMappings(Staff staff) {
    if (staff.getId() == null) {
      return;
    }
    if (!mappingRepository.findByStaffId(staff.getId()).isEmpty()) {
      return;
    }

    List<String> classes = splitClasses(staff.getAssignedClasses());
    if (classes.isEmpty()) {
      return;
    }
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
