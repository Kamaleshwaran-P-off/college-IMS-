package com.smartcampus.platform.notification.broadcast.dto;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;

public class BroadcastMarkReadRequest {
  @NotEmpty
  private List<Long> ids;

  public List<Long> getIds() {
    return ids;
  }

  public void setIds(List<Long> ids) {
    this.ids = ids;
  }
}
