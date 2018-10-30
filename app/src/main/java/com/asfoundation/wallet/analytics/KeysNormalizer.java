package com.asfoundation.wallet.analytics;

import cm.aptoide.analytics.KeyValueNormalizer;
import java.util.HashMap;
import java.util.Map;

public class KeysNormalizer implements KeyValueNormalizer {

  @Override public Map<String, Object> normalize(Map<String, Object> data) {
    Map<String, Object> normalized = new HashMap<>();
    for (Map.Entry<String, Object> entrySet : data.entrySet()) {
      if (entrySet.getValue() != null) {
        if (entrySet.getValue()
            .getClass()
            .isInstance(new HashMap())) {
          normalized.put(entrySet.getKey(), normalize((HashMap) entrySet.getValue()));
        } else {
          normalized.put(entrySet.getKey(), entrySet.getValue());
        }
      }
    }
    return normalized;
  }
}
