package com.smartcampus.platform.planner.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class StudyPlanMultiRequest extends StudyPlanRequest {
  @NotNull
  @Min(1)
  @Max(8)
  private Integer weeks;

  public Integer getWeeks() {
    return weeks;
  }

  public void setWeeks(Integer weeks) {
    this.weeks = weeks;
  }
}
