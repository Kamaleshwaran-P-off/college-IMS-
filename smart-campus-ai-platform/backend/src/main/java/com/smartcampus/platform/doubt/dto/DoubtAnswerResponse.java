package com.smartcampus.platform.doubt.dto;

import java.time.LocalDateTime;

public class DoubtAnswerResponse {
  private Long id;
  private Long authorId;
  private String authorName;
  private String authorRole;
  private String content;
  private LocalDateTime createdAt;

  public DoubtAnswerResponse(
      Long id,
      Long authorId,
      String authorName,
      String authorRole,
      String content,
      LocalDateTime createdAt
  ) {
    this.id = id;
    this.authorId = authorId;
    this.authorName = authorName;
    this.authorRole = authorRole;
    this.content = content;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
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
