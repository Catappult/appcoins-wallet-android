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
  private final AppEventsLogger api;

  public FacebookEventLogger(AppEventsLogger api) {
    this.api = api;
  }


  private Bundle flatten (Map<String, Object> data) {
    Bundle bundle = new Bundle();
    for (Map.Entry<String, Object> entry : data.entrySet()) {
      if (entry.getValue()
          .getClass()
          .isInstance(new HashMap())) {
        flatten((HashMap) entry.getValue());
      }
      else {
        bundle.putString(entry.getKey(), entry.getValue().toString());
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

    api.logEvent(eventName, bundle);

    /*api.logEvent(action, eventName, new AnalyticsBody(BuildConfig.VERSION_CODE, BuildConfig.APPLICATION_ID, data))
        .subscribeOn(Schedulers.io())
        .subscribe(() -> Log.d(TAG, "event sent"), Throwable::printStackTrace);*/

  }

  @Override public void setup() {

  }
}
