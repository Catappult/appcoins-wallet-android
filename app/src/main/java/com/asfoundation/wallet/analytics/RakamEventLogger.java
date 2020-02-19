package com.asfoundation.wallet.analytics;

import android.util.Log;
import cm.aptoide.analytics.AnalyticsManager;
import cm.aptoide.analytics.EventLogger;
import io.rakam.api.Rakam;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class RakamEventLogger implements EventLogger {

  private static final String TAG = "RakamEventLogger";

  public RakamEventLogger() {
  }

  @Override
  public void log(String eventName, Map<String, Object> data, AnalyticsManager.Action action,
      String context) {
    if (data != null) {
      Rakam.getInstance()
          .logEvent(eventName, mapToJsonObject(data));
    } else {
      Rakam.getInstance()
          .logEvent(eventName);
    }
    Log.d(TAG, "log() called with: "
        + "eventName = ["
        + eventName
        + "], data = ["
        + data
        + "], action = ["
        + action
        + "], context = ["
        + context
        + "]");
  }

  @Override public void setup() {

  }

  private JSONObject mapToJsonObject(Map<String, Object> data) {
    JSONObject eventData = new JSONObject();
    for (Map.Entry<String, Object> entry : data.entrySet()) {
      if (entry.getValue() != null) {
        try {
          eventData.put(entry.getKey(), entry.getValue()
              .toString());
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    }
    return eventData;
  }
}
