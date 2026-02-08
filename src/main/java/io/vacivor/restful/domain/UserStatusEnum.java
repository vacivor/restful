package io.vacivor.restful.domain;

public enum UserStatusEnum {
  DISABLED(100),
  ENABLED(200);

  private final int code;

  UserStatusEnum(int code) {
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  public static UserStatusEnum fromCode(Integer code) {
    if (code == null) {
      return null;
    }
    for (UserStatusEnum status : values()) {
      if (status.code == code) {
        return status;
      }
    }
    throw new IllegalArgumentException("Unknown status code: " + code);
  }
}
