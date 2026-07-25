package com.smartcampus.platform.coursework.material.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.coursework.material.dto.StudyMaterialResponse;
import com.smartcampus.platform.coursework.material.entity.StudyMaterial;
import com.smartcampus.platform.coursework.material.repository.StudyMaterialRepository;
import com.smartcampus.platform.defaultdata.RealisticDataGenerator;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.staff.repository.FacultyClassMappingRepository;
import com.smartcampus.platform.staff.service.StaffProfileService;
import com.smartcampus.platform.student.entity.Student;
import com.smartcampus.platform.student.service.StudentProfileService;

@Service
@Transactional
public class StudyMaterialService {
  private final StudyMaterialRepository materialRepository;
  private final UserRepository userRepository;
  private final StaffProfileService staffProfileService;
  private final StudentProfileService studentProfileService;
  private final FacultyClassMappingRepository mappingRepository;
  private final RealisticDataGenerator dataGenerator;

  public StudyMaterialService(
      StudyMaterialRepository materialRepository,
      UserRepository userRepository,
      StaffProfileService staffProfileService,
      StudentProfileService studentProfileService,
      FacultyClassMappingRepository mappingRepository,
      RealisticDataGenerator dataGenerator
  ) {
    this.materialRepository = materialRepository;
    this.userRepository = userRepository;
    this.staffProfileService = staffProfileService;
    this.studentProfileService = studentProfileService;
    this.mappingRepository = mappingRepository;
    this.dataGenerator = dataGenerator;
  }

  public StudyMaterialResponse uploadMaterial(
      String title,
      String description,
      String department,
      String className,
      MultipartFile file,
      String facultyEmail
  ) throws IOException {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }

    Staff staff = staffProfileService.ensureForUser(user);

    String normalizedClass = normalize(className);
    assertClassAccessIfNeeded(staff, normalizedClass);

    String normalizedDepartment = normalize(department);
    if (normalizedDepartment == null && normalizedClass != null) {
      normalizedDepartment = extractDepartment(normalizedClass);
    }

    LocalDateTime now = LocalDateTime.now();
    StudyMaterial material = new StudyMaterial(
        staff,
        title,
        description,
        normalizedDepartment,
        normalizedClass,
        file != null ? file.getOriginalFilename() : null,
        file != null ? file.getContentType() : null,
        file != null ? file.getBytes() : null,
        now,
        true,
        now
    );

    StudyMaterial saved = materialRepository.save(material);
    return toResponse(saved);
  }

  public List<StudyMaterialResponse> getMaterials(String email) {
    var user = userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    if (user.getRole() == Role.STUDENT) {
      Student student = studentProfileService.ensureForUser(user);
      String className = normalize(buildClassKey(student.getDepartment(), student.getSection()));

      List<StudyMaterialResponse> classMaterials = materialRepository
          .findByClassNameAndIsVisibleTrueOrderByUploadedAtDesc(className)
          .stream()
          .map(this::toResponse)
          .toList();
      List<StudyMaterialResponse> globalMaterials = materialRepository
          .findByClassNameIsNullAndIsVisibleTrueOrderByUploadedAtDesc()
          .stream()
          .map(this::toResponse)
          .toList();

      List<StudyMaterialResponse> combined = new java.util.ArrayList<>(classMaterials);
      combined.addAll(globalMaterials);
      return combined.isEmpty() ? dataGenerator.getDefaultStudyMaterials() : combined;
    }

    if (user.getRole() == Role.STAFF) {
      Staff staff = staffProfileService.ensureForUser(user);
      List<String> assignedClasses = getAssignedClasses(staff);
      return materialRepository.findByUploadedByIdOrderByUploadedAtDesc(staff.getId())
          .stream()
          .filter(material -> {
            String materialClass = normalize(material.getClassName());
            if (materialClass == null) {
              return true;
            }
            return assignedClasses.stream().anyMatch(value -> value.equalsIgnoreCase(materialClass));
          })
          .map(this::toResponse)
          .collect(java.util.stream.Collectors.collectingAndThen(java.util.stream.Collectors.toList(),
              list -> list.isEmpty() ? dataGenerator.getDefaultStudyMaterials() : list));
    }

    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
  }

  public StudyMaterial getMaterial(Long id) {
    return materialRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Material not found"));
  }

  public StudyMaterialResponse updateMaterial(
      Long id,
      String title,
      String description,
      String department,
      String className,
      MultipartFile file,
      String facultyEmail
  ) throws IOException {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }

    Staff staff = staffProfileService.ensureForUser(user);
    StudyMaterial material = getMaterial(id);
    if (!material.getUploadedBy().getId().equals(staff.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty can edit only their materials");
    }
    assertClassAccessIfNeeded(staff, material.getClassName());

    String normalizedClass = normalize(className);
    if (normalizedClass != null) {
      assertClassAccessIfNeeded(staff, normalizedClass);
      material.setClassName(normalizedClass);
    }

    if (title != null && !title.isBlank()) {
      material.setTitle(title);
    }
    if (description != null) {
      material.setDescription(description);
    }
    if (department != null) {
      material.setDepartment(normalize(department));
    } else if (normalizedClass != null && (material.getDepartment() == null || material.getDepartment().isBlank())) {
      material.setDepartment(extractDepartment(normalizedClass));
    }
    if (file != null && !file.isEmpty()) {
      material.setFileName(file.getOriginalFilename());
      material.setContentType(file.getContentType());
      material.setFileData(file.getBytes());
    }
    material.setUpdatedAt(LocalDateTime.now());
    return toResponse(materialRepository.save(material));
  }

  public StudyMaterialResponse updateVisibility(Long id, Boolean visible, String facultyEmail) {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }

    Staff staff = staffProfileService.ensureForUser(user);
    StudyMaterial material = getMaterial(id);
    if (!material.getUploadedBy().getId().equals(staff.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty can update only their materials");
    }
    assertClassAccessIfNeeded(staff, material.getClassName());

    boolean nextValue = visible != null ? visible : !Boolean.TRUE.equals(material.getIsVisible());
    material.setIsVisible(nextValue);
    material.setUpdatedAt(LocalDateTime.now());
    return toResponse(materialRepository.save(material));
  }

  public void deleteMaterial(Long id, String facultyEmail) {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }

    Staff staff = staffProfileService.ensureForUser(user);
    StudyMaterial material = getMaterial(id);
    if (!material.getUploadedBy().getId().equals(staff.getId())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty can delete only their materials");
    }
    assertClassAccessIfNeeded(staff, material.getClassName());
    materialRepository.delete(material);
  }

  public StudyMaterial getMaterialForDownload(Long materialId, String requesterEmail) {
    var user = userRepository.findByEmail(requesterEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

    StudyMaterial material = getMaterial(materialId);

    if (user.getRole() == Role.STUDENT) {
      Student student = studentProfileService.ensureForUser(user);
      String studentClass = normalize(buildClassKey(student.getDepartment(), student.getSection()));
      String materialClass = normalize(material.getClassName());
      if (materialClass != null && (studentClass == null || !materialClass.equalsIgnoreCase(studentClass))) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
      }
      if (!Boolean.TRUE.equals(material.getIsVisible())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
      }
      return material;
    }

    if (user.getRole() == Role.STAFF) {
      Staff staff = staffProfileService.ensureForUser(user);
      if (!material.getUploadedBy().getId().equals(staff.getId())) {
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
      }
      assertClassAccessIfNeeded(staff, material.getClassName());
      return material;
    }

    throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
  }

  private StudyMaterialResponse toResponse(StudyMaterial material) {
    return new StudyMaterialResponse(
        material.getId(),
        material.getTitle(),
        material.getDescription(),
        material.getDepartment(),
        material.getClassName(),
        material.getUploadedBy().getUser().getFullName(),
        material.getUploadedAt(),
        material.getUpdatedAt(),
        material.getFileData() != null,
        Boolean.TRUE.equals(material.getIsVisible())
    );
  }

  private String buildClassKey(String department, String section) {
    if (department == null || section == null) {
      return null;
    }
    return department.trim() + "-" + section.trim();
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

  private String extractDepartment(String className) {
    if (className == null) {
      return null;
    }
    int dashIndex = className.lastIndexOf("-");
    if (dashIndex > 0) {
      return normalize(className.substring(0, dashIndex));
    }
    return normalize(className);
  }

  private List<String> getAssignedClasses(Staff staff) {
    List<String> mapped = mappingRepository.findByStaffId(staff.getId()).stream()
        .map(mapping -> normalize(mapping.getClassName()))
        .filter(Objects::nonNull)
        .toList();
    if (!mapped.isEmpty()) {
      return mapped;
    }
    if (staff.getAssignedClasses() == null || staff.getAssignedClasses().isBlank()) {
      return List.of();
    }
    return Arrays.stream(staff.getAssignedClasses().split(","))
        .map(this::normalize)
        .filter(Objects::nonNull)
        .toList();
  }

  private void assertClassAccessIfNeeded(Staff staff, String className) {
    String normalized = normalize(className);
    if (normalized == null) {
      return;
    }
    boolean allowed = getAssignedClasses(staff).stream()
        .anyMatch(value -> value.equalsIgnoreCase(normalized));
    if (!allowed) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty is not assigned to this class");
    }
  }
}
