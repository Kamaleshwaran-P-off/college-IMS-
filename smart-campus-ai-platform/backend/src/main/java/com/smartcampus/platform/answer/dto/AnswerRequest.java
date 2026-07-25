package com.smartcampus.platform.answer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class AnswerRequest {
  @NotNull
  private Long doubtId;

  @NotNull
  private Long authorId;

  @NotBlank
  private String content;

  public Long getDoubtId() {
    return doubtId;
  }

  public void setDoubtId(Long doubtId) {
    this.doubtId = doubtId;
  }

  public Long getAuthorId() {
    return authorId;
  }

  public void setAuthorId(Long authorId) {
    this.authorId = authorId;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }
}
