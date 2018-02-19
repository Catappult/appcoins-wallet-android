package com.asf.wallet.router;

import android.content.Intent;

public class Result {
  private final boolean success;
  private final int requestCode;
  private final Intent data;

  public Result(boolean success, int requestCode, Intent data) {
    this.success = success;
    this.requestCode = requestCode;
    this.data = data;
  }

  public boolean isSuccess() {
    return success;
  }

  public int getRequestCode() {
    return requestCode;
  }

  public Intent getData() {
    return data;
  }
}
