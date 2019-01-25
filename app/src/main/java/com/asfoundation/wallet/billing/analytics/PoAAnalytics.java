package com.asfoundation.wallet.billing.analytics;

import cm.aptoide.analytics.AnalyticsManager;

public class PoAAnalytics implements PoAEventSender {

  private static final String EVENT_PACKAGE_NAME = "packageName";
  private static final String EVENT_CAMPAIGN_ID = "campaignId";
  private static final String EVENT_NETWORK_ID = "networkId";
  private static final String WALLET = "WALLET";
  public static final String POA_STARTED = "POA_STARTED";

  private final AnalyticsManager analytics;

  public PoAAnalytics(AnalyticsManager analytics) {
    this.analytics = analytics;
  }

  @Override
  public void sendPoAStartedEvent(String packageName, String campaignId, String networkId) {
  }
}