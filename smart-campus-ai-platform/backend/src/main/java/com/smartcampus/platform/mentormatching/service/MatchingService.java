package com.smartcampus.platform.mentormatching.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.mentormatching.dto.MentorMatchResponse;
import com.smartcampus.platform.mentormatching.entity.FacultySkill;
import com.smartcampus.platform.mentormatching.entity.MentorMatch;
import com.smartcampus.platform.mentormatching.entity.ProficiencyLevel;
import com.smartcampus.platform.mentormatching.entity.StudentPreference;
import com.smartcampus.platform.mentormatching.repository.FacultySkillRepository;
import com.smartcampus.platform.mentormatching.repository.MentorMatchRepository;
import com.smartcampus.platform.mentormatching.repository.StudentPreferenceRepository;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.student.entity.Student;
import com.smartcampus.platform.student.repository.StudentRepository;
import com.smartcampus.platform.student.service.StudentProfileService;

@Service
@Transactional
public class MatchingService {
  private final UserRepository userRepository;
  private final StudentProfileService studentProfileService;
  private final StudentPreferenceRepository preferenceRepository;
  private final FacultySkillRepository facultySkillRepository;
  private final MentorMatchRepository matchRepository;
  private final StudentRepository studentRepository;

  public MatchingService(
      UserRepository userRepository,
      StudentProfileService studentProfileService,
      StudentPreferenceRepository preferenceRepository,
      FacultySkillRepository facultySkillRepository,
      MentorMatchRepository matchRepository,
      StudentRepository studentRepository
  ) {
    this.userRepository = userRepository;
    this.studentProfileService = studentProfileService;
    this.preferenceRepository = preferenceRepository;
    this.facultySkillRepository = facultySkillRepository;
    this.matchRepository = matchRepository;
    this.studentRepository = studentRepository;
  }

  public List<MentorMatchResponse> getTopMentors(String studentEmail) {
    var user = userRepository.findByEmail(studentEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STUDENT) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Student access required");
    }

    Student student = studentProfileService.ensureForUser(user);
    return runMatching(student);
  }

  public List<MentorMatchResponse> runForStudent(Long studentId, String adminEmail) {
    var user = userRepository.findByEmail(adminEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
    }

    Student student = studentRepository.findById(studentId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
    return runMatching(student);
  }

  private List<MentorMatchResponse> runMatching(Student student) {
    StudentPreference preference = preferenceRepository.findByStudentId(student.getId())
        .orElse(null);

    List<FacultySkill> skills = facultySkillRepository.findAll().stream()
        .filter(skill -> skill.getStaff() != null && skill.getStaff().getUser() != null)
        .filter(skill -> {
          if (student.getDepartment() == null || student.getDepartment().isBlank()) {
            return true;
          }
          String staffDept = skill.getStaff().getDepartment();
          return staffDept != null && staffDept.equalsIgnoreCase(student.getDepartment());
        })
        .toList();

    List<MentorScore> scored = new ArrayList<>();
    for (FacultySkill skill : skills) {
      double score = calculateMatchScore(preference, skill);
      scored.add(new MentorScore(skill, score));
    }

    List<MentorScore> top = scored.stream()
        .sorted(Comparator.comparing(MentorScore::score).reversed())
        .limit(5)
        .toList();

    matchRepository.deleteByStudentId(student.getId());
    LocalDateTime now = LocalDateTime.now();
    List<MentorMatch> matches = top.stream()
        .map(entry -> new MentorMatch(student, entry.skill().getStaff(), entry.score(), now))
        .toList();
    matchRepository.saveAll(matches);

    return top.stream().map(entry -> toResponse(entry.skill(), entry.score())).toList();
  }

  public double calculateMatchScore(StudentPreference preference, FacultySkill skill) {
    double skillScore = calculateSkillScore(preference, skill);
    double availabilityScore = calculateAvailabilityScore(preference, skill);
    double experienceScore = calculateExperienceScore(skill);
    double preferenceScore = calculatePreferenceScore(preference, skill);

    return roundToTwoDecimals(
        (skillScore * 0.5) +
        (availabilityScore * 0.2) +
        (experienceScore * 0.2) +
        (preferenceScore * 0.1)
    );
  }

  private double calculateSkillScore(StudentPreference preference, FacultySkill skill) {
    if (preference == null) {
      return 0;
    }
    Set<String> required = splitTags(preference.getRequiredSkills());
    if (required.isEmpty()) {
      return 0;
    }
    Set<String> mentorSkills = splitTags(skill.getSkills());
    if (mentorSkills.isEmpty()) {
      return 0;
    }
    long matches = required.stream().filter(mentorSkills::contains).count();
    return matches / (double) required.size();
  }

  private double calculateAvailabilityScore(StudentPreference preference, FacultySkill skill) {
    if (preference == null) {
      return 0;
    }
    Set<String> studentSlots = splitTags(preference.getAvailability());
    Set<String> mentorSlots = splitTags(skill.getAvailability());
    if (studentSlots.isEmpty() || mentorSlots.isEmpty()) {
      return 0;
    }
    long matches = studentSlots.stream().filter(mentorSlots::contains).count();
    return matches / (double) studentSlots.size();
  }

  private double calculateExperienceScore(FacultySkill skill) {
    ProficiencyLevel level = skill.getProficiencyLevel();
    if (level == null) {
      return 0.3;
    }
    return switch (level) {
      case BEGINNER -> 0.4;
      case INTERMEDIATE -> 0.7;
      case EXPERT -> 1.0;
    };
  }

  private double calculatePreferenceScore(StudentPreference preference, FacultySkill skill) {
    if (preference == null) {
      return 0;
    }
    String mentorType = normalize(preference.getMentorType());
    if (mentorType == null) {
      return 0;
    }
    ProficiencyLevel level = skill.getProficiencyLevel();
    if (level == null) {
      return 0;
    }
    return mentorType.contains(level.name().toLowerCase(Locale.ROOT)) ? 1 : 0;
  }

  private MentorMatchResponse toResponse(FacultySkill skill, Double score) {
    Staff staff = skill.getStaff();
    return new MentorMatchResponse(
        staff.getId(),
        staff.getUser().getFullName(),
        staff.getDepartment(),
        skill.getSkills(),
        skill.getProficiencyLevel(),
        skill.getAvailability(),
        skill.getBio(),
        score
    );
  }

  private Set<String> splitTags(String raw) {
    if (raw == null || raw.isBlank()) {
      return Set.of();
    }
    return java.util.Arrays.stream(raw.toLowerCase(Locale.ROOT).split(","))
        .map(String::trim)
        .filter(value -> !value.isBlank())
        .collect(Collectors.toSet());
  }

  private String normalize(String raw) {
    if (raw == null || raw.isBlank()) {
      return null;
    }
    return raw.trim().toLowerCase(Locale.ROOT);
  }

  private double roundToTwoDecimals(double value) {
    return Math.round(value * 100.0) / 100.0;
  }

  private record MentorScore(FacultySkill skill, Double score) {
    public MentorScore {
      Objects.requireNonNull(skill);
      Objects.requireNonNull(score);
    }
  }
}
