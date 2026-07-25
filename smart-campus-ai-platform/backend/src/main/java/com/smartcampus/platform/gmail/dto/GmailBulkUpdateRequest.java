package com.smartcampus.platform.gmail.dto;

import java.util.List;

public class GmailBulkUpdateRequest {
  private List<String> ids;
  private String category;
  private Boolean important;

  public GmailBulkUpdateRequest() {}

  public List<String> getIds() {
    return ids;
  }

  public void setIds(List<String> ids) {
    this.ids = ids;
  }

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
