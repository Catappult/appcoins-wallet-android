package com.asfoundation.wallet.analytics;

import android.util.Log;
import cm.aptoide.analytics.AnalyticsManager;
import cm.aptoide.analytics.EventLogger;
import com.asf.wallet.BuildConfig;
import io.reactivex.schedulers.Schedulers;
import java.util.Map;

public class BackendEventLogger implements EventLogger {

  public static final String TAG = AnalyticsManager.class.getSimpleName();
  private final AnalyticsAPI api;

  public BackendEventLogger(AnalyticsAPI api) {
    this.api = api;
  }

  @Override
  public void log(String eventName, Map<String, Object> data, AnalyticsManager.Action action,
      String context) {
    Log.d(TAG, "log() called with: eventName = ["
        + eventName
        + "], data = ["
        + data
        + "], action = ["
        + action
        + "], context = ["
        + context
        + "]");

    api.registerEvent(action, eventName, new AnalyticsBody(BuildConfig.VERSION_CODE, BuildConfig.APPLICATION_ID, data))
        .subscribeOn(Schedulers.io())
        .subscribe(() -> Log.d(TAG, "event sent"), Throwable::printStackTrace);

  }

  @Override public void setup() {
    Log.d(AnalyticsManager.class.getSimpleName(), "setup() called");
  }
}
