package com.smartcampus.platform.emailintelligence.dto;

import java.util.List;

public class EmailInsightPageResponse {
  private List<EmailInsightResponse> items;
  private int page;
  private int size;
  private long total;
  private int totalPages;

  public EmailInsightPageResponse() {}

  public EmailInsightPageResponse(List<EmailInsightResponse> items, int page, int size, long total, int totalPages) {
    this.items = items;
    this.page = page;
    this.size = size;
    this.total = total;
    this.totalPages = totalPages;
  }

  public List<EmailInsightResponse> getItems() {
    return items;
  }

  public void setItems(List<EmailInsightResponse> items) {
    this.items = items;
  }

  public int getPage() {
    return page;
  }

  public void setPage(int page) {
    this.page = page;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public long getTotal() {
    return total;
  }

  public void setTotal(long total) {
    this.total = total;
  }

  public int getTotalPages() {
    return totalPages;
  }

  public void setTotalPages(int totalPages) {
    this.totalPages = totalPages;
  }
}
