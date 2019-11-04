package com.asfoundation.wallet;

import com.asf.wallet.BuildConfig;
import com.flurry.android.FlurryAgent;

public class FlurryLogger implements Logger {

  @Override public void log(Throwable throwable) {
    throwable.printStackTrace();
    if (!BuildConfig.DEBUG) {
      FlurryAgent.onError("ID", throwable.getMessage(), throwable);
    }
  }
}
