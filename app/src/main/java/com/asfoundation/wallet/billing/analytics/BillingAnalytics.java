package com.asfoundation.wallet.billing.analytics;

import java.util.HashMap;
import java.util.Map;
import cm.aptoide.analytics.AnalyticsManager;

public class BillingAnalytics implements EventSender {
  private static final String WALLET = "WALLET";
  public static final String PURCHASE_DETAILS = "PURCHASE_DETAILS";
  public static final String CREDIT_CARD_DETAILS = "CREDIT_CARD_DETAILS";
  public static final String PAYMENT = "PAYMENT";
  private static final String EVENT_PACKAGE_NAME= "package_name";
  private static final String EVENT_SKU = "sku";
  private static final String EVENT_VALUE = "value";
  private static final String EVENT_PURCHASE= "purchase";
  private static final String EVENT_PAYMENT_METHOD = "payment_method";
  public static final String PAYMENT_METHOD_APPC = "APPC";
  public static final String PAYMENT_METHOD_CC = "CREDIT_CARD";
  public static final String PAYMENT_METHOD_REWARDS = "REWARDS";

  private final AnalyticsManager analytics;

  public BillingAnalytics(AnalyticsManager analytics) {
    this.analytics = analytics;
  }

  @Override
  public void sendPurchaseDetailsEvent(String packageName, String skuDetails, String value,
      String purchaseDetail) {
    Map<String, Object> map = new HashMap<>();
    Map<String, Object> map2 = new HashMap<>();

    map2.put(EVENT_PACKAGE_NAME, packageName);
    map2.put(EVENT_SKU, skuDetails);
    map2.put(EVENT_VALUE, value);

    map.put(EVENT_PURCHASE, map2);
    map.put(EVENT_PAYMENT_METHOD, purchaseDetail);

    analytics.logEvent(map, PURCHASE_DETAILS, AnalyticsManager.Action.CLICK, WALLET);
  }

  @Override
  public void sendCreditCardDetailsEvent(String packageName, String skuDetails, String value) {
    Map<String, Object> map = new HashMap<>();
    Map<String, Object> map2 = new HashMap<>();

    map2.put(EVENT_PACKAGE_NAME, packageName);
    map2.put(EVENT_SKU, skuDetails);
    map2.put(EVENT_VALUE, value);

    map.put(EVENT_PURCHASE, map2);

    analytics.logEvent(map, CREDIT_CARD_DETAILS, AnalyticsManager.Action.CLICK, WALLET);
  }

  @Override public void sendPaymentEvent(String packageName, String skuDetails, String value, String purchaseDetail) {
    Map<String, Object> map = new HashMap<>();
    Map<String, Object> map2 = new HashMap<>();

    map2.put(EVENT_PACKAGE_NAME, packageName);
    map2.put(EVENT_SKU, skuDetails);
    map2.put(EVENT_VALUE, value);

    map.put(EVENT_PURCHASE, map2);
    map.put(EVENT_PAYMENT_METHOD, purchaseDetail);

    analytics.logEvent(map, PAYMENT, AnalyticsManager.Action.IMPRESSION, WALLET);
  }
}