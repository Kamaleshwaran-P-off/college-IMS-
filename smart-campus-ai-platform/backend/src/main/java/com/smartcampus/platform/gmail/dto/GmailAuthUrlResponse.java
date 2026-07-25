package com.smartcampus.platform.gmail.dto;

public class GmailAuthUrlResponse {
  private String url;

  public GmailAuthUrlResponse() {}

  public GmailAuthUrlResponse(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
