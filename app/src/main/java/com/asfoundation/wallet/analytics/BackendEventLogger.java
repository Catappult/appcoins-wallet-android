package com.asfoundation.wallet.analytics;

import cm.aptoide.analytics.AnalyticsManager;
import cm.aptoide.analytics.AnalyticsManager.Action;
import cm.aptoide.analytics.EventLogger;
import com.appcoins.wallet.core.network.analytics.AnalyticsAPI;
import com.appcoins.wallet.core.network.analytics.AnalyticsBody;
import com.asf.wallet.BuildConfig;
import com.appcoins.wallet.core.utils.android_common.Log;
import io.reactivex.schedulers.Schedulers;
import java.util.Map;

public class BackendEventLogger implements EventLogger {

  private static final String TAG = AnalyticsManager.class.getSimpleName();
  private final AnalyticsAPI api;

  public BackendEventLogger(AnalyticsAPI api) {
    this.api = api;
  }

  @Override
  public void log(String eventName, Map<String, Object> data, Action action, String context) {
    Log.d(TAG, "log() called with: eventName = ["
        + eventName
        + "], data = ["
        + data
        + "], action = ["
        + action
        + "], context = ["
        + context
        + "]");

    api.registerEvent(action, eventName,
        new AnalyticsBody(BuildConfig.VERSION_CODE, BuildConfig.APPLICATION_ID, data))
        .subscribeOn(Schedulers.io())
        .subscribe(() -> Log.d(TAG, "event sent"), Throwable::printStackTrace);
  }

  @Override public void setup() {
    Log.d(AnalyticsManager.class.getSimpleName(), "setup() called");
  }
}
