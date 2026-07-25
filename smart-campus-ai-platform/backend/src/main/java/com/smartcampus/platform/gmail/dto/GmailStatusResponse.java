package com.smartcampus.platform.gmail.dto;

public class GmailStatusResponse {
  private boolean linked;

  public GmailStatusResponse() {}

  public GmailStatusResponse(boolean linked) {
    this.linked = linked;
  }

  public boolean isLinked() {
    return linked;
  }

  public void setLinked(boolean linked) {
    this.linked = linked;
  }
}
