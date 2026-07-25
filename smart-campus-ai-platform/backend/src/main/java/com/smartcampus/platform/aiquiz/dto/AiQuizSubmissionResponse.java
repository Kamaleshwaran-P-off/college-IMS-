package com.smartcampus.platform.aiquiz.dto;

public class AiQuizSubmissionResponse {
  private Long submissionId;
  private int score;
  private int total;
  private double percentage;
  private Integer timeTakenSeconds;
  private String certificateUrl;

  public AiQuizSubmissionResponse() {}

  public AiQuizSubmissionResponse(
      Long submissionId,
      int score,
      int total,
      double percentage,
      Integer timeTakenSeconds,
      String certificateUrl
  ) {
    this.submissionId = submissionId;
    this.score = score;
    this.total = total;
    this.percentage = percentage;
    this.timeTakenSeconds = timeTakenSeconds;
    this.certificateUrl = certificateUrl;
  }

  public Long getSubmissionId() {
    return submissionId;
  }

  public int getScore() {
    return score;
  }

  public int getTotal() {
    return total;
  }

  public double getPercentage() {
    return percentage;
  }

  public Integer getTimeTakenSeconds() {
    return timeTakenSeconds;
  }

  public String getCertificateUrl() {
    return certificateUrl;
  }
}
