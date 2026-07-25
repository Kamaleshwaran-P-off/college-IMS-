package com.smartcampus.platform.answer.dto;

import java.time.LocalDateTime;

public class AnswerResponse {
  private Long id;
  private Long doubtId;
  private Long authorId;
  private String authorName;
  private String authorRole;
  private String content;
  private LocalDateTime createdAt;

  public AnswerResponse(
      Long id,
      Long doubtId,
      Long authorId,
      String authorName,
      String authorRole,
      String content,
      LocalDateTime createdAt
  ) {
    this.id = id;
    this.doubtId = doubtId;
    this.authorId = authorId;
    this.authorName = authorName;
    this.authorRole = authorRole;
    this.content = content;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public Long getDoubtId() {
    return doubtId;
  }

  public Long getAuthorId() {
    return authorId;
  }

  public String getAuthorName() {
    return authorName;
  }

  public String getAuthorRole() {
    return authorRole;
  }

  public String getContent() {
    return content;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }
}
