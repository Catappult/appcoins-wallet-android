package com.asfoundation.wallet.analytics;

import android.util.Log;
import cm.aptoide.analytics.AnalyticsLogger;
import com.asf.wallet.BuildConfig;

public class LogcatAnalyticsLogger implements AnalyticsLogger {
  @Override public void logDebug(String tag, String msg) {
    if (BuildConfig.LOGGABLE) {
      Log.d(tag, msg);
    }
  }

  @Override public void logWarningDebug(String TAG, String msg) {
    Log.w(TAG, msg);
  }
}
