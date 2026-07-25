package com.smartcampus.platform.planner.dto;

public class StudyTaskResponse {
  private Long id;
  private int dayOrder;
  private String dayLabel;
  private String title;
  private String details;
  private boolean completed;
  private String reminderAt;

  public StudyTaskResponse(
      Long id,
      int dayOrder,
      String dayLabel,
      String title,
      String details,
      boolean completed,
      String reminderAt
  ) {
    this.id = id;
    this.dayOrder = dayOrder;
    this.dayLabel = dayLabel;
    this.title = title;
    this.details = details;
    this.completed = completed;
    this.reminderAt = reminderAt;
  }

  public Long getId() {
    return id;
  }

  public int getDayOrder() {
    return dayOrder;
  }

  public String getDayLabel() {
    return dayLabel;
  }

  public String getTitle() {
    return title;
  }

  public String getDetails() {
    return details;
  }

  public boolean isCompleted() {
    return completed;
  }

  public String getReminderAt() {
    return reminderAt;
  }
}
