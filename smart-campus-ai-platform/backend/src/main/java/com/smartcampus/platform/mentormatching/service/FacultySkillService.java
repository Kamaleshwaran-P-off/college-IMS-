package com.smartcampus.platform.mentormatching.service;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.smartcampus.platform.auth.entity.Role;
import com.smartcampus.platform.auth.repository.UserRepository;
import com.smartcampus.platform.mentormatching.dto.FacultySkillRequest;
import com.smartcampus.platform.mentormatching.dto.FacultySkillResponse;
import com.smartcampus.platform.mentormatching.entity.FacultySkill;
import com.smartcampus.platform.mentormatching.repository.FacultySkillRepository;
import com.smartcampus.platform.staff.entity.Staff;
import com.smartcampus.platform.staff.service.StaffProfileService;

@Service
@Transactional
public class FacultySkillService {
  private final FacultySkillRepository facultySkillRepository;
  private final UserRepository userRepository;
  private final StaffProfileService staffProfileService;

  public FacultySkillService(
      FacultySkillRepository facultySkillRepository,
      UserRepository userRepository,
      StaffProfileService staffProfileService
  ) {
    this.facultySkillRepository = facultySkillRepository;
    this.userRepository = userRepository;
    this.staffProfileService = staffProfileService;
  }

  public FacultySkillResponse upsert(String facultyEmail, FacultySkillRequest request) {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }

    Staff staff = staffProfileService.ensureForUser(user);

    FacultySkill skill = facultySkillRepository.findByStaffId(staff.getId())
        .orElseGet(() -> new FacultySkill(staff, null, null, null, null, LocalDateTime.now()));

    skill.setSkills(request.getSkills());
    skill.setProficiencyLevel(request.getProficiencyLevel());
    skill.setAvailability(request.getAvailability());
    skill.setBio(request.getBio());
    skill.setUpdatedAt(LocalDateTime.now());

    FacultySkill saved = facultySkillRepository.save(skill);
    return toResponse(saved);
  }

  public FacultySkillResponse getProfile(String facultyEmail) {
    var user = userRepository.findByEmail(facultyEmail)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    if (user.getRole() != Role.STAFF) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Faculty access required");
    }
    Staff staff = staffProfileService.ensureForUser(user);

    FacultySkill skill = facultySkillRepository.findByStaffId(staff.getId())
        .orElseGet(() -> new FacultySkill(staff, "", null, "", "", LocalDateTime.now()));
    return toResponse(skill);
  }

  private FacultySkillResponse toResponse(FacultySkill skill) {
    return new FacultySkillResponse(
        skill.getId(),
        skill.getStaff().getId(),
        skill.getStaff().getUser().getFullName(),
        skill.getStaff().getDepartment(),
        skill.getSkills(),
        skill.getProficiencyLevel(),
        skill.getAvailability(),
        skill.getBio()
    );
  }
}
