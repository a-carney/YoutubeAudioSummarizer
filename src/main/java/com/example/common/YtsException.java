package com.example.common;

import lombok.Getter;

@Getter
public class YtsException extends RuntimeException {

  private final YtsErrorType ytsErrorType;

  public YtsException(String msg, Exception ex) {
    super(msg, ex);
    this.ytsErrorType = YtsErrorType.PROCESSING;
  }

  public YtsException(String msg) {
    super(msg);
    this.ytsErrorType = YtsErrorType.PROCESSING;
  }

  public YtsException(String msg, YtsErrorType ytsErrorType) {
    super(msg);
    this.ytsErrorType = ytsErrorType;
  }

  public static YtsException get(String msg, YtsErrorType ytsErrorType) {
    String label = switch (ytsErrorType) {
      case PROCESSING -> "[PROCESS ERROR] - ";
      case VALIDATION -> "[VALIDATION ERROR] - ";
    };
    return new YtsException(label + msg, ytsErrorType);
  }
}
