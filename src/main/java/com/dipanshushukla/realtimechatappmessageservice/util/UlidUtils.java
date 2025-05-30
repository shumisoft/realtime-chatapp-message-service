package com.dipanshushukla.realtimechatappmessageservice.util;

import com.github.f4b6a3.ulid.Ulid;

public class UlidUtils {

  private UlidUtils() {
  }

  public static String toString(byte[] bytes) {
    return bytes == null ? null : Ulid.from(bytes).toString();
  }

  public static byte[] toBytes(String ulid) {
    return ulid == null ? null : Ulid.from(ulid).toBytes();
  }

}
