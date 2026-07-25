package com.smartcampus.platform.inbox.dto;

import java.util.List;

import com.smartcampus.platform.inbox.EmailCategory;

public class InboxBulkUpdateRequest {
  private List<Long> ids;
  private EmailCategory category;
  private Boolean important;

  public InboxBulkUpdateRequest() {}

  public InboxBulkUpdateRequest(List<Long> ids, EmailCategory category, Boolean important) {
    this.ids = ids;
    this.category = category;
    this.important = important;
  }

  public List<Long> getIds() {
    return ids;
  }

  public void setIds(List<Long> ids) {
    this.ids = ids;
  }

  public EmailCategory getCategory() {
    return category;
  }

  public void setCategory(EmailCategory category) {
    this.category = category;
  }

  public Boolean getImportant() {
    return important;
  }

  public void setImportant(Boolean important) {
    this.important = important;
  }
}
