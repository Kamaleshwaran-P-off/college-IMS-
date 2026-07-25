package com.smartcampus.platform.profile.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.entity.User;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.mentormatching.entity.FacultySkill;
import com.smartcampus.platform.mentormatching.entity.ProficiencyLevel;
import com.smartcampus.platform.mentormatching.entity.StudentPreference;
import com.smartcampus.platform.mentormatching.repository.FacultySkillRepository;
import com.smartcampus.platform.mentormatching.repository.StudentPreferenceRepository;
import com.smartcampus.platform.profile.dto.UserProfileResponse;
import com.smartcampus.platform.profile.dto.UserProfileUpdateRequest;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.staff.repository.StaffRepository;
import com.smartcampus.platform.staff.service.StaffProfileService;
import com.smartcampus.platform.student.entity.Student;
import com.smartcampus.platform.student.repository.StudentRepository;
import com.smartcampus.platform.student.service.StudentProfileService;

@Service
@Transactional
public class UserProfileService {
  private final UserRepository userRepository;
  private final StudentProfileService studentProfileService;
  private final StudentRepository studentRepository;
  private final StaffProfileService staffProfileService;
  private final StaffRepository staffRepository;
  private final StudentPreferenceRepository preferenceRepository;
  private final FacultySkillRepository facultySkillRepository;

  public UserProfileService(
      UserRepository userRepository,
      StudentProfileService studentProfileService,
      StudentRepository studentRepository,
      StaffProfileService staffProfileService,
      StaffRepository staffRepository,
      StudentPreferenceRepository preferenceRepository,
      FacultySkillRepository facultySkillRepository
  ) {
    this.userRepository = userRepository;
    this.studentProfileService = studentProfileService;
    this.studentRepository = studentRepository;
    this.staffProfileService = staffProfileService;
    this.staffRepository = staffRepository;
    this.preferenceRepository = preferenceRepository;
    this.facultySkillRepository = facultySkillRepository;
  }

  public UserProfileResponse getProfile(String email) {
    User user = loadUser(email);
    if (user.getRole() == Role.STUDENT) {
      Student student = studentProfileService.ensureForUser(user);
      StudentPreference preference = preferenceRepository.findByStudentId(student.getId())
          .orElse(null);
      List<String> interestedSkills = splitTags(preference != null ? preference.getRequiredSkills() : null);
      return buildStudentResponse(user, student, interestedSkills);
    }
    if (user.getRole() == Role.STAFF) {
      Staff staff = staffProfileService.ensureForUser(user);
      FacultySkill skill = facultySkillRepository.findByStaffId(staff.getId())
          .orElse(null);
      List<String> skills = splitTags(skill != null ? skill.getSkills() : staff.getSkills());
      String bio = skill != null ? skill.getBio() : null;
      return buildFacultyResponse(user, staff, skills, bio);
    }
    return buildBaseResponse(user);
  }

  public UserProfileResponse updateProfile(String email, UserProfileUpdateRequest request) {
    User user = loadUser(email);

    if (request.getName() != null && !request.getName().isBlank()) {
      user.setFullName(request.getName().trim());
    }

    if (user.getRole() == Role.STUDENT) {
      Student student = studentProfileService.ensureForUser(user);
      if (request.getRegisterNumber() != null && !request.getRegisterNumber().isBlank()) {
        student.setStudentCode(request.getRegisterNumber().trim());
      }
      if (request.getDepartment() != null) {
        student.setDepartment(clean(request.getDepartment()));
      }
      if (request.getSection() != null) {
        student.setSection(clean(request.getSection()));
      }
      if (request.getPhone() != null) {
        student.setPhone(clean(request.getPhone()));
      }
      studentRepository.save(student);

      if (request.getInterestedSkills() != null) {
        StudentPreference preference = preferenceRepository.findByStudentId(student.getId())
            .orElseGet(() -> new StudentPreference(student, "", "", "", "", LocalDateTime.now()));
        preference.setRequiredSkills(joinTags(request.getInterestedSkills()));
        preference.setUpdatedAt(LocalDateTime.now());
        preferenceRepository.save(preference);
      }
    }

    if (user.getRole() == Role.STAFF) {
      Staff staff = staffProfileService.ensureForUser(user);
      if (request.getStaffId() != null && !request.getStaffId().isBlank()) {
        staff.setStaffCode(request.getStaffId().trim());
      }
      if (request.getDepartment() != null) {
        staff.setDepartment(clean(request.getDepartment()));
      }
      if (request.getPhone() != null) {
        staff.setPhone(clean(request.getPhone()));
      }
      if (request.getExperienceYears() != null) {
        staff.setExperienceYears(request.getExperienceYears());
      }
      staffRepository.save(staff);

      if (request.getSkills() != null || request.getBio() != null || request.getExperienceYears() != null) {
        FacultySkill skill = facultySkillRepository.findByStaffId(staff.getId())
            .orElseGet(() -> new FacultySkill(staff, "", null, "", "", LocalDateTime.now()));
        if (request.getSkills() != null) {
          String skills = joinTags(request.getSkills());
          skill.setSkills(skills);
          staff.setSkills(skills);
        }
        if (request.getBio() != null) {
          skill.setBio(request.getBio());
        }
        if (request.getExperienceYears() != null) {
          skill.setProficiencyLevel(mapExperience(request.getExperienceYears()));
        }
        skill.setUpdatedAt(LocalDateTime.now());
        facultySkillRepository.save(skill);
      }
    }

    userRepository.save(user);
    return getProfile(email);
  }

  public User updateProfileImage(String email, String contentType, byte[] imageData) {
    User user = loadUser(email);
    user.setProfileImageData(imageData);
    user.setProfileImageContentType(contentType);
    user.setProfileImageUrl("/api/user/profile/image/" + user.getId());
    return userRepository.save(user);
  }

  public User loadUserForImage(Long id, String requesterEmail) {
    User requester = loadUser(requesterEmail);
    User target = userRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    if (!target.getId().equals(requester.getId()) && requester.getRole() != Role.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");
    }
    return target;
  }

  private User loadUser(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
  }

  private UserProfileResponse buildStudentResponse(User user, Student student, List<String> interestedSkills) {
    return new UserProfileResponse(
        user.getId(),
        user.getFullName(),
        user.getEmail(),
        user.getRole().name(),
        resolveImageUrl(user),
        new UserProfileResponse.StudentProfileDetails(
            student.getStudentCode(),
            student.getPhone(),
            student.getDepartment(),
            student.getSection(),
            interestedSkills
        ),
        null
    );
  }

  private UserProfileResponse buildFacultyResponse(User user, Staff staff, List<String> skills, String bio) {
    return new UserProfileResponse(
        user.getId(),
        user.getFullName(),
        user.getEmail(),
        user.getRole().name(),
        resolveImageUrl(user),
        null,
        new UserProfileResponse.FacultyProfileDetails(
            staff.getStaffCode(),
            staff.getDepartment(),
            skills,
            staff.getExperienceYears(),
            bio
        )
    );
  }

  private UserProfileResponse buildBaseResponse(User user) {
    return new UserProfileResponse(
        user.getId(),
        user.getFullName(),
        user.getEmail(),
        user.getRole().name(),
        resolveImageUrl(user),
        null,
        null
    );
  }

  private String resolveImageUrl(User user) {
    if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isBlank()) {
      return user.getProfileImageUrl();
    }
    if (user.getProfileImageData() != null && user.getProfileImageData().length > 0) {
      return "/api/user/profile/image/" + user.getId();
    }
    return null;
  }

  private String joinTags(List<String> tags) {
    return tags.stream()
        .map(String::trim)
        .filter(value -> !value.isBlank())
        .collect(Collectors.joining(","));
  }

  private List<String> splitTags(String raw) {
    if (raw == null || raw.isBlank()) {
      return new ArrayList<>();
    }
    return java.util.Arrays.stream(raw.split(","))
        .map(String::trim)
        .filter(value -> !value.isBlank())
        .collect(Collectors.toList());
  }

  private String clean(String value) {
    if (value == null) return null;
    String trimmed = value.trim();
    return trimmed.isBlank() ? null : trimmed;
  }

  private ProficiencyLevel mapExperience(Integer years) {
    if (years == null) {
      return null;
    }
    if (years <= 2) {
      return ProficiencyLevel.BEGINNER;
    }
    if (years <= 5) {
      return ProficiencyLevel.INTERMEDIATE;
    }
    return ProficiencyLevel.EXPERT;
  }
}
