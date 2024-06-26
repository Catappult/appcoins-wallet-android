package com.appcoins.wallet.core.analytics.analytics;

import cm.aptoide.analytics.AnalyticsLogger;
import com.appcoins.wallet.core.analytics.analytics.logging.Log;

public class LogcatAnalyticsLogger implements AnalyticsLogger {
  @Override public void logDebug(String tag, String msg) {
    Log.d(tag, msg);
  }

  @Override public void logWarningDebug(String TAG, String msg) {
    Log.w(TAG, msg);
  }
}
