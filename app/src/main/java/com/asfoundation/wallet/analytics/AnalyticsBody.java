package com.asfoundation.wallet.analytics;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

public class AnalyticsBody {

  @JsonProperty("aptoide_vercode") private int appcoinsVercode;
  @JsonProperty("aptoide_package") private String
      appcoinsPackage;
  private Map data;

  public AnalyticsBody(int appcoinsVercode, String appcoinsPackage, Map data) {
    this.appcoinsVercode = appcoinsVercode;
    this.appcoinsPackage = appcoinsPackage;
    this.data = data;
  }

  public int getAppcoinsVercode() {
    return appcoinsVercode;
  }

  public String getAppcoinsPackage() {
    return appcoinsPackage;
  }

  public Map getData() {
    return data;
  }
}
