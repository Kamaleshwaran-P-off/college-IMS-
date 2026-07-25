package com.smartcampus.platform.happenstance.dto;

import java.util.List;

public class HappenstanceOpportunityResponse {
  private Long id;
  private String title;
  private String description;
  private String domain;
  private String link;
  private String platform;
  private String type;
  private List<String> tags;
  private String date;
  private String location;
  private boolean trending;
  private boolean saved;

  public HappenstanceOpportunityResponse(
      Long id,
      String title,
      String description,
      String domain,
      String link,
      String platform,
      String type,
      List<String> tags,
      String date,
      String location,
      boolean trending,
      boolean saved
  ) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.domain = domain;
    this.link = link;
    this.platform = platform;
    this.type = type;
    this.tags = tags;
    this.date = date;
    this.location = location;
    this.trending = trending;
    this.saved = saved;
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public String getDomain() {
    return domain;
  }

  public String getLink() {
    return link;
  }

  public String getPlatform() {
    return platform;
  }

  public String getType() {
    return type;
  }

  public List<String> getTags() {
    return tags;
  }

  public String getDate() {
    return date;
  }

  public String getLocation() {
    return location;
  }

  public boolean isTrending() {
    return trending;
  }

  public boolean isSaved() {
    return saved;
  }
}
