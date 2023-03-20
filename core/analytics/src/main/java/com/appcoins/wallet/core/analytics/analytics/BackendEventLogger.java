package com.appcoins.wallet.core.analytics.analytics;

import com.appcoins.wallet.core.analytics.analytics.logging.Log;

import cm.aptoide.analytics.AnalyticsManager;
import cm.aptoide.analytics.AnalyticsManager.Action;
import cm.aptoide.analytics.EventLogger;
import com.appcoins.wallet.core.network.analytics.AnalyticsAPI;
import com.appcoins.wallet.core.network.analytics.AnalyticsBody;
import io.reactivex.schedulers.Schedulers;
import java.util.Map;

public class BackendEventLogger implements EventLogger {

  private static final String TAG = AnalyticsManager.class.getSimpleName();
  private final AnalyticsAPI api;

  private final int versionCode;
  private final String applicationId;


  public BackendEventLogger(AnalyticsAPI api, int versionCode, String applicationId  ) {
    this.api = api;
    this.versionCode = versionCode;
    this.applicationId = applicationId;
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
        new AnalyticsBody(versionCode, applicationId, data))
        .subscribeOn(Schedulers.io())
        .subscribe(() -> Log.d(TAG, "event sent"), Throwable::printStackTrace);
  }

  @Override public void setup() {
    Log.d(AnalyticsManager.class.getSimpleName(), "setup() called");
  }
}
