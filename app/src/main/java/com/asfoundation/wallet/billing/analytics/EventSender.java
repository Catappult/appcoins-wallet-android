package com.asfoundation.wallet.billing.analytics;

public interface EventSender {

  void sendPurchaseDetailsEvent(String packageName, String skuDetails, String value,
      String transactionType);

  void sendPaymentMethodDetailsEvent(String packageName, String skuDetails, String value,
      String purchaseDetails, String transactionType);

  void sendActionPaymentMethodDetailsActionEvent(String packageName, String skuDetails,
      String value, String purchaseDetails, String transactionType, String action);

  void sendPaymentEvent(String packageName, String skuDetails, String value, String purchaseDetails,
      String transactionType);

  void sendRevenueEvent(String value);

  void sendPreSelectedPaymentMethodEvent(String packageName, String skuDetails, String value,
      String purchaseDetails, String transactionType, String action);

  void sendPaymentMethodEvent(String packageName, String skuDetails, String value,
      String purchaseDetails, String transactionType, String action);

  void sendPaymentConfirmationEvent(String packageName, String skuDetails, String value,
      String purchaseDetails, String transactionType, String action);

  void sendPaymentErrorEvent(String packageName, String skuDetails, String value,
      String purchaseDetails, String transactionType, String errorCode);

  void sendPaymentErrorWithDetailsEvent(String packageName, String skuDetails, String value,
      String purchaseDetails, String transactionType, String errorCode, String errorDetails);

  void sendPaymentErrorWithDetailsAndRiskEvent(String packageName, String skuDetails, String value,
      String purchaseDetails, String transactionType, String errorCode, String errorDetails,
      String riskRules);

  void sendPaymentSuccessEvent(String packageName, String skuDetails, String value,
      String purchaseDetails, String transactionType);

  void sendPaymentPendingEvent(String packageName, String skuDetails, String value,
      String purchaseDetails, String transactionType);

  void sendPurchaseStartEvent(String packageName, String skuDetails, String value,
      String purchaseDetails, String transactionType, String context);

  void sendPurchaseStartWithoutDetailsEvent(String packageName, String skuDetails, String value,
      String transactionType, String context);

  void sendPaypalUrlEvent(String packageName, String skuDetails, String value,
      String transactionType, String type, String resultCode, String url);
}