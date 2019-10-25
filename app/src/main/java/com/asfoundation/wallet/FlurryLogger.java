package com.asfoundation.wallet;

import com.flurry.android.FlurryAgent;

public class FlurryLogger implements Logger {
  @Override public void log(Throwable throwable) {
    throwable.printStackTrace();
    FlurryAgent.onError("ID", throwable.getMessage(), throwable);
  }
}
