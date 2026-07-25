package com.smartcampus.platform.happenstance.dto;

public class HappenstanceOpportunityStat {
  private Long id;
  private String title;
  private long saves;

  public HappenstanceOpportunityStat(Long id, String title, long saves) {
    this.id = id;
    this.title = title;
    this.saves = saves;
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public long getSaves() {
    return saves;
  }
}
