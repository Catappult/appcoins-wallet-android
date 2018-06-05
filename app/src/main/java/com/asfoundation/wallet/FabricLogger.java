package com.asfoundation.wallet;

import com.crashlytics.android.Crashlytics;

public class FabricLogger implements Logger {
  @Override public void log(Throwable throwable) {
    throwable.printStackTrace();
    if (Crashlytics.getInstance() != null) {
      Crashlytics.logException(throwable);
    }
  }
}
