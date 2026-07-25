package com.smartcampus.platform.gmail.dto;

public class GmailUpdateRequest {
  private String category;
  private Boolean important;

  public GmailUpdateRequest() {}

  public String getCategory() {
    return category;
  }

  public void setCategory(String category) {
    this.category = category;
  }

  public Boolean getImportant() {
    return important;
  }

  public void setImportant(Boolean important) {
    this.important = important;
  }
}
