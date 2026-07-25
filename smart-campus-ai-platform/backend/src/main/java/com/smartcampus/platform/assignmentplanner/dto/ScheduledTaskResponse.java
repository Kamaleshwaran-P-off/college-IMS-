package com.smartcampus.platform.assignmentplanner.dto;

import java.time.LocalDate;

public record ScheduledTaskResponse(
    Long id,
    Long assignmentId,
    String assignmentTitle,
    LocalDate date,
    String taskDetail,
    Double hours,
    boolean completed,
    LocalDate deadline
) {}
