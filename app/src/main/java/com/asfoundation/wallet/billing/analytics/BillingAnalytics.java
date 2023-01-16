package com.asfoundation.wallet.billing.analytics;

import cm.aptoide.analytics.AnalyticsManager;
import it.czerwinski.android.hilt.annotations.BoundTo;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;

@BoundTo(supertype = EventSender.class) public class BillingAnalytics implements EventSender {
  public static final String PURCHASE_DETAILS = "PURCHASE_DETAILS";
  public static final String PAYMENT_METHOD_DETAILS = "PAYMENT_METHOD_DETAILS";
  public static final String PAYMENT = "PAYMENT";
  public static final String REVENUE = "REVENUE";
  public static final String PAYMENT_METHOD_APPC = "APPC";
  public static final String PAYMENT_METHOD_CC = "CREDIT_CARD";
  public static final String PAYMENT_METHOD_REWARDS = "REWARDS";
  public static final String PAYMENT_METHOD_PAYPAL = "PAYPAL";
  public static final String PAYMENT_METHOD_PAYPALV2 = "PAYPAL_V2";
  public static final String PAYMENT_METHOD_GOOGLE_PAY = "GOOGLE_PAY";
  public static final String PAYMENT_METHOD_CARRIER = "CARRIER";
  public static final String RAKAM_PRESELECTED_PAYMENT_METHOD = "wallet_preselected_payment_method";
  public static final String RAKAM_PAYMENT_METHOD = "wallet_payment_method";
  public static final String RAKAM_PAYMENT_CONFIRMATION = "wallet_payment_confirmation";
  public static final String RAKAM_PAYMENT_CONCLUSION = "wallet_payment_conclusion";
  public static final String RAKAM_PAYMENT_START = "wallet_payment_start";
  public static final String RAKAM_PAYPAL_URL = "wallet_payment_conclusion_paypal";
  public static final String RAKAM_PAYMENT_METHOD_DETAILS = "wallet_payment_method_details";
  public static final String RAKAM_PAYMENT_BILLING = "wallet_payment_billing";
  public static final String EVENT_REVENUE_CURRENCY = "EUR";
  private static final String WALLET = "WALLET";
  private static final String EVENT_PACKAGE_NAME = "package_name";
  private static final String EVENT_SKU = "sku";
  private static final String EVENT_VALUE = "value";
  private static final String EVENT_PURCHASE = "purchase";
  private static final String EVENT_TRANSACTION_TYPE = "transaction_type";
  private static final String EVENT_PAYMENT_METHOD = "payment_method";
  private static final String EVENT_ACTION = "action";
  private static final String EVENT_CONTEXT = "context";
  private static final String EVENT_STATUS = "status";
  private static final String EVENT_ERROR_CODE = "error_code";
  private static final String EVENT_ERROR_DETAILS = "error_details";
  private static final String EVENT_CODE_RISK_RULES = "error_code_risk_rule";
  private static final String EVENT_SUCCESS = "success";
  private static final String EVENT_FAIL = "fail";
  private static final String EVENT_PENDING = "pending";
  private static final String EVENT_PAYPAL_TYPE = "type";
  private static final String EVENT_RESULT_CODE = "result_code";
  private static final String EVENT_URL = "url";
  private static final int MAX_CHARACTERS = 100;
  private final AnalyticsManager analytics;

  public @Inject BillingAnalytics(AnalyticsManager analytics) {
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
  public void sendActionPaymentMethodDetailsActionEvent(String packageName, String skuDetails,
      String value, String purchaseDetails, String transactionType, String action) {
    Map<String, Object> eventData =
        createBaseRakamEventMap(packageName, skuDetails, value, purchaseDetails, transactionType,
            action);

    analytics.logEvent(eventData, RAKAM_PAYMENT_METHOD_DETAILS, AnalyticsManager.Action.CLICK,
        WALLET);
  }

  @Override public void sendPaymentEvent(String packageName, String skuDetails, String value,
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

  @Override public void sendRevenueEvent(String value) {
    Map<String, Object> eventData = new HashMap<>();

    eventData.put(EVENT_VALUE, value);

    analytics.logEvent(eventData, REVENUE, AnalyticsManager.Action.IMPRESSION, WALLET);
  }

  @Override
  public void sendPreSelectedPaymentMethodEvent(String packageName, String skuDetails, String value,
      String purchaseDetails, String transactionType, String action) {
    Map<String, Object> eventData =
        createBaseRakamEventMap(packageName, skuDetails, value, purchaseDetails, transactionType,
            action);

    analytics.logEvent(eventData, RAKAM_PRESELECTED_PAYMENT_METHOD, AnalyticsManager.Action.CLICK,
        WALLET);
  }

  @Override public void sendPaymentMethodEvent(String packageName, String skuDetails, String value,
      String purchaseDetails, String transactionType, String action) {
    Map<String, Object> eventData =
        createBaseRakamEventMap(packageName, skuDetails, value, purchaseDetails, transactionType,
            action);

    analytics.logEvent(eventData, RAKAM_PAYMENT_METHOD, AnalyticsManager.Action.CLICK, WALLET);
  }

  @Override
  public void sendPaymentConfirmationEvent(String packageName, String skuDetails, String value,
      String purchaseDetails, String transactionType, String action) {
    Map<String, Object> eventData =
        createBaseRakamEventMap(packageName, skuDetails, value, purchaseDetails, transactionType,
            action);

    analytics.logEvent(eventData, RAKAM_PAYMENT_CONFIRMATION, AnalyticsManager.Action.CLICK,
        WALLET);
  }

  @Override public void sendPaymentErrorEvent(String packageName, String skuDetails, String value,
      String purchaseDetails, String transactionType, String errorCode) {
    Map<String, Object> eventData =
        createConclusionRakamEventMap(packageName, skuDetails, value, purchaseDetails,
            transactionType, EVENT_FAIL);

    eventData.put(EVENT_ERROR_CODE, errorCode);

    analytics.logEvent(eventData, RAKAM_PAYMENT_CONCLUSION, AnalyticsManager.Action.CLICK, WALLET);
  }

  @Override
  public void sendPaymentErrorWithDetailsEvent(String packageName, String skuDetails, String value,
      String purchaseDetails, String transactionType, String errorCode, String errorDetails) {
    Map<String, Object> eventData =
        createConclusionRakamEventMap(packageName, skuDetails, value, purchaseDetails,
            transactionType, EVENT_FAIL);

    eventData.put(EVENT_ERROR_CODE, errorCode);
    eventData.put(EVENT_ERROR_DETAILS, errorDetails);

    analytics.logEvent(eventData, RAKAM_PAYMENT_CONCLUSION, AnalyticsManager.Action.CLICK, WALLET);
  }

  @Override
  public void sendPaymentErrorWithDetailsAndRiskEvent(String packageName, String skuDetails,
      String value, String purchaseDetails, String transactionType, String errorCode,
      String errorDetails, String riskRules) {
    Map<String, Object> eventData =
        createConclusionRakamEventMap(packageName, skuDetails, value, purchaseDetails,
            transactionType, EVENT_FAIL);

    eventData.put(EVENT_ERROR_CODE, errorCode);
    eventData.put(EVENT_ERROR_DETAILS, errorDetails);
    if (riskRules != null) eventData.put(EVENT_CODE_RISK_RULES, riskRules);

    analytics.logEvent(eventData, RAKAM_PAYMENT_CONCLUSION, AnalyticsManager.Action.CLICK, WALLET);
  }

  @Override public void sendPaymentSuccessEvent(String packageName, String skuDetails, String value,
      String purchaseDetails, String transactionType) {
    Map<String, Object> eventData =
        createConclusionRakamEventMap(packageName, skuDetails, value, purchaseDetails,
            transactionType, EVENT_SUCCESS);

    analytics.logEvent(eventData, RAKAM_PAYMENT_CONCLUSION, AnalyticsManager.Action.CLICK, WALLET);
  }

  @Override public void sendPaymentPendingEvent(String packageName, String skuDetails, String value,
      String purchaseDetails, String transactionType) {
    Map<String, Object> eventData =
        createConclusionRakamEventMap(packageName, skuDetails, value, purchaseDetails,
            transactionType, EVENT_PENDING);

    analytics.logEvent(eventData, RAKAM_PAYMENT_CONCLUSION, AnalyticsManager.Action.CLICK, WALLET);
  }

  @Override public void sendPurchaseStartEvent(String packageName, String skuDetails, String value,
      String purchaseDetails, String transactionType, String context) {
    Map<String, Object> eventData = new HashMap<>();

    eventData.put(EVENT_PACKAGE_NAME, packageName);
    eventData.put(EVENT_SKU, skuDetails);
    eventData.put(EVENT_VALUE, value);
    eventData.put(EVENT_TRANSACTION_TYPE, transactionType);
    eventData.put(EVENT_PAYMENT_METHOD, purchaseDetails);
    eventData.put(EVENT_CONTEXT, context);

    analytics.logEvent(eventData, RAKAM_PAYMENT_START, AnalyticsManager.Action.CLICK, WALLET);
  }

  @Override public void sendPurchaseStartWithoutDetailsEvent(String packageName, String skuDetails,
      String value, String transactionType, String context) {
    Map<String, Object> eventData = new HashMap<>();

    eventData.put(EVENT_PACKAGE_NAME, packageName);
    eventData.put(EVENT_SKU, skuDetails);
    eventData.put(EVENT_VALUE, value);
    eventData.put(EVENT_TRANSACTION_TYPE, transactionType);
    eventData.put(EVENT_CONTEXT, context);

    analytics.logEvent(eventData, RAKAM_PAYMENT_START, AnalyticsManager.Action.CLICK, WALLET);
  }

  @Override public void sendPaypalUrlEvent(String packageName, String skuDetails, String value,
      String transactionType, String type, String resultCode, String url) {
    Map<String, Object> eventData = new HashMap<>();

    eventData.put(EVENT_PACKAGE_NAME, packageName);
    eventData.put(EVENT_SKU, skuDetails);
    eventData.put(EVENT_VALUE, value);
    eventData.put(EVENT_TRANSACTION_TYPE, transactionType);
    eventData.put(EVENT_PAYPAL_TYPE, type);
    eventData.put(EVENT_RESULT_CODE, resultCode);
    if (url.length() > MAX_CHARACTERS) {
      eventData.put(EVENT_URL, url.substring(url.length() - MAX_CHARACTERS));
    } else {
      eventData.put(EVENT_URL, url);
    }

    analytics.logEvent(eventData, RAKAM_PAYPAL_URL, AnalyticsManager.Action.CLICK, WALLET);
  }

  public void sendBillingAddressActionEvent(String packageName, String skuDetails, String value,
      String purchaseDetails, String transactionType, String action) {
    Map<String, Object> eventData =
        createBaseRakamEventMap(packageName, skuDetails, value, purchaseDetails, transactionType,
            action);

    analytics.logEvent(eventData, RAKAM_PAYMENT_BILLING, AnalyticsManager.Action.CLICK, WALLET);
  }

  private Map<String, Object> createBaseRakamEventMap(String packageName, String skuDetails,
      String value, String purchaseDetails, String transactionType, String action) {
    Map<String, Object> eventData = new HashMap<>();

    eventData.put(EVENT_PACKAGE_NAME, packageName);
    eventData.put(EVENT_SKU, skuDetails);
    eventData.put(EVENT_VALUE, value);
    eventData.put(EVENT_TRANSACTION_TYPE, transactionType);
    eventData.put(EVENT_PAYMENT_METHOD, purchaseDetails);
    eventData.put(EVENT_ACTION, action);

    return eventData;
  }

  private Map<String, Object> createConclusionRakamEventMap(String packageName, String skuDetails,
      String value, String purchaseDetails, String transactionType, String status) {
    Map<String, Object> eventData = new HashMap<>();

    eventData.put(EVENT_PACKAGE_NAME, packageName);
    eventData.put(EVENT_SKU, skuDetails);
    eventData.put(EVENT_VALUE, value);
    eventData.put(EVENT_TRANSACTION_TYPE, transactionType);
    eventData.put(EVENT_PAYMENT_METHOD, purchaseDetails);
    eventData.put(EVENT_STATUS, status);

    return eventData;
  }
}