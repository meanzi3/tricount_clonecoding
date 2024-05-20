package com.example.tricount.exception;

public class ForbiddenAccessException extends RuntimeException {
  public ForbiddenAccessException() {
  }

  public ForbiddenAccessException(String message) {
    super(message);
  }

  public ForbiddenAccessException(String message, Throwable cause) {
    super(message, cause);
  }

  public ForbiddenAccessException(Throwable cause) {
    super(cause);
  }

  public ForbiddenAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}

