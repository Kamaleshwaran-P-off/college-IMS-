package com.smartcampus.platform.carousel.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(name = "carousel_images")
public class CarouselImage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String fileName;

  @Column(nullable = false)
  private String contentType;

  @Lob
  @Column(nullable = false, columnDefinition = "LONGBLOB")
  private byte[] imageData;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  public CarouselImage() {}

  public CarouselImage(String fileName, String contentType, byte[] imageData, LocalDateTime createdAt) {
    this.fileName = fileName;
    this.contentType = contentType;
    this.imageData = imageData;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public byte[] getImageData() {
    return imageData;
  }

  public void setImageData(byte[] imageData) {
    this.imageData = imageData;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
