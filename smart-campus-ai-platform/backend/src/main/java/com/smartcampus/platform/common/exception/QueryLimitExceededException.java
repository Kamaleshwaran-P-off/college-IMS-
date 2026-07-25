package com.smartcampus.platform.common.exception;

public class QueryLimitExceededException extends RuntimeException {
  public QueryLimitExceededException(String message) {
    super(message);
  }
}
