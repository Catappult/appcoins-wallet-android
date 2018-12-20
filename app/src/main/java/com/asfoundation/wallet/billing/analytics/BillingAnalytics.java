package com.asfoundation.wallet.billing.analytics;

import java.util.HashMap;
import java.util.Map;
import cm.aptoide.analytics.AnalyticsManager;

public class BillingAnalytics implements EventSender {
  private static final String WALLET = "WALLET";
  public static final String PURCHASE_DETAILS = "PURCHASE_DETAILS";
  public static final String PAYMENT_METHOD_DETAILS = "PAYMENT_METHOD_DETAILS";
  public static final String PAYMENT = "PAYMENT";
  public static final String REVENUE = "REVENUE";
  private static final String EVENT_PACKAGE_NAME = "package_name";
  private static final String EVENT_SKU = "sku";
  private static final String EVENT_VALUE = "value";
  private static final String EVENT_PURCHASE = "purchase";
  private static final String EVENT_TRANSACTION_TYPE = "transaction_type";
  private static final String EVENT_PAYMENT_METHOD = "payment_method";
  public static final String PAYMENT_METHOD_APPC = "APPC";
  public static final String PAYMENT_METHOD_CC = "CREDIT_CARD";
  public static final String PAYMENT_METHOD_REWARDS = "REWARDS";
  public static final String PAYMENT_METHOD_PAYPAL = "PAYPAL";

  private final AnalyticsManager analytics;

  public BillingAnalytics(AnalyticsManager analytics) {
    this.analytics = analytics;
  }

  @Override
  public void sendPurchaseDetailsEvent(String packageName, String skuDetails, String value,
      String transactionType) {
    Map<String, Object> eventData = new HashMap<>();
    Map<String, Object> purchaseData = new HashMap<>();

    purchaseData.put(EVENT_PACKAGE_NAME, packageName);
    purchaseData.put(EVENT_SKU, skuDetails);
    purchaseData.put(EVENT_VALUE, value);

    eventData.put(EVENT_PURCHASE, purchaseData);
    eventData.put(EVENT_TRANSACTION_TYPE, transactionType);

    analytics.logEvent(eventData, PURCHASE_DETAILS, AnalyticsManager.Action.CLICK, WALLET);
  }

  @Override
  public void sendPaymentMethodDetailsEvent(String packageName, String skuDetails, String value,
      String purchaseDetails, String transactionType) {
    Map<String, Object> eventData = new HashMap<>();
    Map<String, Object> purchaseData = new HashMap<>();

    purchaseData.put(EVENT_PACKAGE_NAME, packageName);
    purchaseData.put(EVENT_SKU, skuDetails);
    purchaseData.put(EVENT_VALUE, value);

    eventData.put(EVENT_PURCHASE, purchaseData);
    eventData.put(EVENT_PAYMENT_METHOD, purchaseDetails);
    eventData.put(EVENT_TRANSACTION_TYPE, transactionType);

    analytics.logEvent(eventData, PAYMENT_METHOD_DETAILS, AnalyticsManager.Action.CLICK, WALLET);
  }

  @Override
  public void sendPaymentEvent(String packageName, String skuDetails, String value,
      String purchaseDetails, String transactionType) {
    Map<String, Object> eventData = new HashMap<>();
    Map<String, Object> purchaseData = new HashMap<>();

    purchaseData.put(EVENT_PACKAGE_NAME, packageName);
    purchaseData.put(EVENT_SKU, skuDetails);
    purchaseData.put(EVENT_VALUE, value);

    eventData.put(EVENT_PURCHASE, purchaseData);
    eventData.put(EVENT_PAYMENT_METHOD, purchaseDetails);
    eventData.put(EVENT_TRANSACTION_TYPE, transactionType);

    analytics.logEvent(eventData, PAYMENT, AnalyticsManager.Action.IMPRESSION, WALLET);
  }

  @Override
  public void sendRevenueEvent(String value) {
    Map<String, Object> eventData = new HashMap<>();

    eventData.put(EVENT_VALUE, value);

    analytics.logEvent(eventData, REVENUE, AnalyticsManager.Action.IMPRESSION, WALLET);
  }
}