package com.smartcampus.platform.chat.dto;

public class ChatResponse {
  private String reply;
  private int remaining;
  private int limit;

  public ChatResponse(String reply, int remaining, int limit) {
    this.reply = reply;
    this.remaining = remaining;
    this.limit = limit;
  }

  public String getReply() {
    return reply;
  }

  public int getRemaining() {
    return remaining;
  }

  public int getLimit() {
    return limit;
  }
}
