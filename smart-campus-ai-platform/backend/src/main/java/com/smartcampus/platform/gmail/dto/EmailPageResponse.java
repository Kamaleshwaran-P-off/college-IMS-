package com.smartcampus.platform.gmail.dto;

import java.util.List;

public class EmailPageResponse {
  private List<StoredEmailResponse> items;
  private int page;
  private int size;
  private long total;
  private int totalPages;

  public EmailPageResponse() {}

  public EmailPageResponse(List<StoredEmailResponse> items, int page, int size, long total, int totalPages) {
    this.items = items;
    this.page = page;
    this.size = size;
    this.total = total;
    this.totalPages = totalPages;
  }

  public List<StoredEmailResponse> getItems() {
    return items;
  }

  public void setItems(List<StoredEmailResponse> items) {
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
