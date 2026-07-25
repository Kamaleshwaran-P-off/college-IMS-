package com.smartcampus.platform.happenstance.dto;

public class HappenstanceDomainStat {
  private String domain;
  private long count;

  public HappenstanceDomainStat(String domain, long count) {
    this.domain = domain;
    this.count = count;
  }

  public String getDomain() {
    return domain;
  }

  public long getCount() {
    return count;
  }
}
