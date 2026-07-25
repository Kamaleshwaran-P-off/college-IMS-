package com.smartcampus.platform.notification.dto;

import jakarta.validation.constraints.NotNull;

public class MarkReadRequest {
  @NotNull
  private Long id;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
