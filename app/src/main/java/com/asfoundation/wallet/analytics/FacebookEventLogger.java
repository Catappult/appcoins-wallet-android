package com.asfoundation.wallet.analytics;

import android.os.Bundle;
import android.util.Log;
import cm.aptoide.analytics.AnalyticsManager;
import cm.aptoide.analytics.EventLogger;
import com.facebook.appevents.AppEventsLogger;
import java.util.HashMap;
import java.util.Map;

public class FacebookEventLogger implements EventLogger {

  public static final String TAG = AnalyticsManager.class.getSimpleName();
  private final AppEventsLogger eventLogger;

  public FacebookEventLogger(AppEventsLogger eventLogger) {
    this.eventLogger = eventLogger;
  }

  private Bundle flatten(Map<String, Object> data) {
    Bundle bundle = new Bundle();
    for (Map.Entry<String, Object> entry : data.entrySet()) {
      if (entry.getValue()
          .getClass()
          .isInstance(new HashMap())) {
        bundle.putAll(flatten((HashMap) entry.getValue()));
      } else {
        bundle.putString(entry.getKey(), entry.getValue()
            .toString());
      }
    }
    return bundle;
  }

  @Override
  public void log(String eventName, Map<String, Object> data, AnalyticsManager.Action action,
      String context) {
    Log.d(TAG, "facebook log() called with: eventName = ["
        + eventName
        + "], data = ["
        + data
        + "], action = ["
        + action
        + "], context = ["
        + context
        + "]");
    Bundle bundle = flatten(data);
    eventLogger.logEvent(eventName, bundle);
  }

  @Override public void setup() {

  }
}
