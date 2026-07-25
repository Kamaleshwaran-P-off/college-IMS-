package com.smartcampus.platform.carousel.dto;

import java.time.LocalDateTime;

public class CarouselItemResponse {
  private Long id;
  private String url;
  private LocalDateTime createdAt;

  public CarouselItemResponse() {}

  public CarouselItemResponse(Long id, String url, LocalDateTime createdAt) {
    this.id = id;
    this.url = url;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
