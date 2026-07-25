package com.smartcampus.platform.assignmentplanner.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.smartcampus.platform.assignmentplanner.entity.AssignmentTargetType;

public record PlannerAssignmentResponse(
    Long id,
    String title,
    String description,
    LocalDate deadline,
    Double estimatedHours,
    AssignmentTargetType targetType,
    Long targetStudentId,
    String targetDepartment,
    String targetSection,
    String createdBy,
    LocalDateTime createdAt
) {}
