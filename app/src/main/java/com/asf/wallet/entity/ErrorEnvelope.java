package com.asf.wallet.entity;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import com.asf.wallet.C;

public class ErrorEnvelope {
  public final int code;
  @Nullable public final String message;
  @Nullable private final Throwable throwable;

  public ErrorEnvelope(@Nullable String message) {
    this(C.ErrorCode.UNKNOWN, message);
  }

  public ErrorEnvelope(int code, @Nullable String message) {
    this(code, message, null);
  }

  public ErrorEnvelope(int code, @StringRes int message) {
    this(code, null, null);
  }

  public ErrorEnvelope(int code, @Nullable String message, @Nullable Throwable throwable) {
    this.code = code;
    this.message = message;
    this.throwable = throwable;
  }
}
