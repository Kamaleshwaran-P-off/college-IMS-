package com.smartcampus.platform.notification.broadcast.entity;

public enum NotificationTargetRole {
  ALL,
  STUDENT,
  STAFF;

  public static NotificationTargetRole from(String value) {
    if (value == null) {
      return null;
    }
    String normalized = value.trim().toUpperCase();
    if (normalized.startsWith("ROLE_")) {
      normalized = normalized.substring(5);
    }
    if ("FACULTY".equals(normalized)) {
      normalized = "STAFF";
    }
    return NotificationTargetRole.valueOf(normalized);
  }
}
