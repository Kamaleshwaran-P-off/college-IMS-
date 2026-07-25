package com.smartcampus.platform.mentormatching.dto;

public class StudentPreferenceResponse {
  private Long id;
  private Long studentId;
  private String studentName;
  private String requiredSkills;
  private String learningGoals;
  private String mentorType;
  private String availability;

  public StudentPreferenceResponse(
      Long id,
      Long studentId,
      String studentName,
      String requiredSkills,
      String learningGoals,
      String mentorType,
      String availability
  ) {
    this.id = id;
    this.studentId = studentId;
    this.studentName = studentName;
    this.requiredSkills = requiredSkills;
    this.learningGoals = learningGoals;
    this.mentorType = mentorType;
    this.availability = availability;
  }

  public Long getId() {
    return id;
  }

  public Long getStudentId() {
    return studentId;
  }

  public String getStudentName() {
    return studentName;
  }

  public String getRequiredSkills() {
    return requiredSkills;
  }

  public String getLearningGoals() {
    return learningGoals;
  }

  public String getMentorType() {
    return mentorType;
  }

  public String getAvailability() {
    return availability;
  }
}
