package com.smartcampus.platform.happenstance.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "happenstance_opportunities")
public class HappenstanceOpportunity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String title;

  @Column(length = 2000)
  private String description;

  @Column(nullable = false)
  private String domain;

  @Column(nullable = false)
  private String link;

  @Column(nullable = false)
  private String platform;

  @Column(nullable = false)
  private String type;

  @Column(length = 1000)
  private String tagsCsv;

  private String dateLabel;

  private String location;

  @Column(nullable = false)
  private boolean trending;

  @Column(nullable = false)
  private LocalDateTime createdAt;

  public HappenstanceOpportunity() {}

  public HappenstanceOpportunity(
      String title,
      String description,
      String domain,
      String link,
      String platform,
      String type,
      String tagsCsv,
      String dateLabel,
      String location,
      boolean trending,
      LocalDateTime createdAt
  ) {
    this.title = title;
    this.description = description;
    this.domain = domain;
    this.link = link;
    this.platform = platform;
    this.type = type;
    this.tagsCsv = tagsCsv;
    this.dateLabel = dateLabel;
    this.location = location;
    this.trending = trending;
    this.createdAt = createdAt;
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

  public String getLink() {
    return link;
  }

  public void setLink(String link) {
    this.link = link;
  }

  public String getPlatform() {
    return platform;
  }

  public void setPlatform(String platform) {
    this.platform = platform;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getTagsCsv() {
    return tagsCsv;
  }

  public void setTagsCsv(String tagsCsv) {
    this.tagsCsv = tagsCsv;
  }

  public String getDateLabel() {
    return dateLabel;
  }

  public void setDateLabel(String dateLabel) {
    this.dateLabel = dateLabel;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public boolean isTrending() {
    return trending;
  }

  public void setTrending(boolean trending) {
    this.trending = trending;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(LocalDateTime createdAt) {
    this.createdAt = createdAt;
  }
}
