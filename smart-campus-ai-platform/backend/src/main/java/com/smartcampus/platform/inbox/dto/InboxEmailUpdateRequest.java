package com.smartcampus.platform.inbox.dto;

import com.smartcampus.platform.inbox.EmailCategory;

public class InboxEmailUpdateRequest {
  private EmailCategory category;
  private Boolean important;

  public InboxEmailUpdateRequest() {}

  public InboxEmailUpdateRequest(EmailCategory category, Boolean important) {
    this.category = category;
    this.important = important;
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
