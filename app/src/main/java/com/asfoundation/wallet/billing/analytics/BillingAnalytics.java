package com.asfoundation.wallet.billing.analytics;

import cm.aptoide.analytics.AnalyticsManager;

public class BillingAnalytics {
  public static final String WALLET = "wallet";
  public static final String PURCHASE_DETAILS = "PURCHASE_DETAILS";
  private final AnalyticsManager analytics;

  public BillingAnalytics(AnalyticsManager analytics) {
    this.analytics = analytics;
  }
}
