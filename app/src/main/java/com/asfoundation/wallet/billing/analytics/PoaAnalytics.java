package com.asfoundation.wallet.billing.analytics;

import android.text.TextUtils;
import cm.aptoide.analytics.AnalyticsManager;
import java.util.HashMap;
import java.util.Map;

public class PoaAnalytics implements PoaEventSender {

  public static final String POA_STARTED = "POA_STARTED";
  public static final String POA_COMPLETED = "POA_COMPLETED";
  public static final String RAKAM_POA_EVENT = "wallet_poa_proof";
  private static final String EVENT_PACKAGE_NAME = "package_name";
  private static final String EVENT_CAMPAIGN_ID = "campaign_id";
  private static final String EVENT_NETWORK_ID = "network_id";
  private static final String EVENT_STATUS = "status";
  private static final String EVENT_ERROR = "error_details";
  private static final String WALLET = "WALLET";

  private final AnalyticsManager analytics;

  public PoaAnalytics(AnalyticsManager analytics) {
    this.analytics = analytics;
  }

  @Override
  public void sendPoaStartedEvent(String packageName, String campaignId, String networkId) {
    Map<String, Object> eventData = new HashMap<>();
    eventData.put(EVENT_PACKAGE_NAME, packageName);
    eventData.put(EVENT_CAMPAIGN_ID, campaignId);
    eventData.put(EVENT_NETWORK_ID, networkId);

    sendRakamProofEvent(packageName, "started", "");
    analytics.logEvent(eventData, POA_STARTED, AnalyticsManager.Action.AUTO, WALLET);
  }

  @Override
  public void sendPoaCompletedEvent(String packageName, String campaignId, String networkId) {
    Map<String, Object> eventData = new HashMap<>();
    eventData.put(EVENT_PACKAGE_NAME, packageName);
    eventData.put(EVENT_CAMPAIGN_ID, campaignId);
    eventData.put(EVENT_NETWORK_ID, networkId);

    analytics.logEvent(eventData, POA_COMPLETED, AnalyticsManager.Action.AUTO, WALLET);
  }

  public void sendRakamProofEvent(String packageName, String status, String error) {
    Map<String, Object> eventData = new HashMap<>();
    eventData.put(EVENT_PACKAGE_NAME, packageName);
    eventData.put(EVENT_STATUS, status);
    if (!TextUtils.isEmpty(error)) eventData.put(EVENT_ERROR, error);

    analytics.logEvent(eventData, RAKAM_POA_EVENT, AnalyticsManager.Action.AUTO, WALLET);
  }
}