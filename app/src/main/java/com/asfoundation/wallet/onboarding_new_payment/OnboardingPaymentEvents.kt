package com.asfoundation.wallet.onboarding_new_payment

import android.content.Intent
import android.net.Uri
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.onboarding_new_payment.use_cases.GetAnalyticsRevenueValueUseCase
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics
import javax.inject.Inject

class OnboardingPaymentEvents @Inject constructor(
  private val paymentMethodsAnalytics: PaymentMethodsAnalytics,
  private val billingAnalytics: BillingAnalytics,
  private val revenueValueUseCase: GetAnalyticsRevenueValueUseCase,
) {


  fun sendPaymentConfirmationEvent(
    transactionBuilder: TransactionBuilder,
    paymentType: PaymentType
  ) {
    billingAnalytics.sendPaymentConfirmationEvent(
      transactionBuilder.domain,
      transactionBuilder.skuId,
      transactionBuilder.amount().toString(),
      paymentType.mapToService().transactionType,
      transactionBuilder.type,
      "buy"
    )
  }

  fun sendPaymentErrorEvent(
    transactionBuilder: TransactionBuilder,
    paymentType: PaymentType,
    refusalCode: Int? =  null,
    refusalReason: String? = null,
    riskRules: String? = null
  ) {
    paymentMethodsAnalytics.stopTimingForPurchaseEvent(
      paymentMethod = paymentType.mapToService().transactionType,
      success = false,
      isPreselected = false
    )
    billingAnalytics.sendPaymentErrorWithDetailsAndRiskEvent(
      transactionBuilder.domain,
      transactionBuilder.skuId,
      transactionBuilder.amount().toString(),
      paymentType.mapToService().transactionType,
      transactionBuilder.type,
      refusalCode.toString(),
      refusalReason,
      riskRules
    )
  }

  fun sendPaymentMethodEvent(
    transactionBuilder: TransactionBuilder,
    paymentType: PaymentType
  ) {
    paymentMethodsAnalytics.sendPaymentMethodEvent(
      transactionBuilder.domain,
      transactionBuilder.skuId,
      transactionBuilder.amount().toString(),
      paymentType.mapToService().transactionType,
      transactionBuilder.type,
      "other_payments"
    )
  }

  fun sendPurchaseStartWithoutDetailsEvent(transactionBuilder: TransactionBuilder) {
    billingAnalytics.sendPurchaseStartWithoutDetailsEvent(
      transactionBuilder.domain,
      transactionBuilder.skuId,
      transactionBuilder.amount().toString(),
      transactionBuilder.type,
      BillingAnalytics.RAKAM_PAYMENT_METHOD
    )
  }

  fun sendPaymentSuccessFinishEvents(
    transactionBuilder: TransactionBuilder,
    paymentType: PaymentType
  ) {
    paymentMethodsAnalytics.stopTimingForPurchaseEvent(
      paymentMethod = paymentType.mapToService().transactionType,
      success = true,
      isPreselected = false
    )
    billingAnalytics.sendPaymentEvent(
      transactionBuilder.domain,
      transactionBuilder.skuId,
      transactionBuilder.amount().toString(),
      paymentType.mapToService().transactionType,
      transactionBuilder.type
    )
    billingAnalytics.sendRevenueEvent(revenueValueUseCase(transactionBuilder))
  }

  fun sendPaymentSuccessEvent(
    transactionBuilder: TransactionBuilder,
    paymentType: PaymentType
  ) {
    billingAnalytics.sendPaymentSuccessEvent(
      transactionBuilder.domain,
      transactionBuilder.skuId,
      transactionBuilder.amount().toString(),
      paymentType.mapToService().transactionType,
      transactionBuilder.type
    )
  }

  fun sendCarrierBillingConfirmationEvent(transactionBuilder: TransactionBuilder, action: String) {
    billingAnalytics.sendPaymentConfirmationEvent(
      transactionBuilder.domain, transactionBuilder.skuId,
      transactionBuilder.amount()
        .toString(), BillingAnalytics.PAYMENT_METHOD_CARRIER,
      transactionBuilder.type, action
    )
  }

  fun sendPayPalConfirmationEvent(transactionBuilder: TransactionBuilder, action: String) {
    billingAnalytics.sendPaymentConfirmationEvent(
      transactionBuilder.domain, transactionBuilder.skuId,
      transactionBuilder.amount()
        .toString(), "paypal",
      transactionBuilder.type, action
    )
  }

  fun sendPaypalUrlEvent(transactionBuilder: TransactionBuilder, data: Intent) {
    val amountString = transactionBuilder.amount()
      .toString()
    billingAnalytics.sendPaypalUrlEvent(
      transactionBuilder.domain, transactionBuilder.skuId,
      amountString, "PAYPAL", getQueryParameter(data, "type"),
      getQueryParameter(data, "resultCode"), data.dataString
    )
  }

  fun getQueryParameter(data: Intent, parameter: String): String? {
    return Uri.parse(data.dataString)
      .getQueryParameter(parameter)
  }

  fun send3dsStart(type: String?) {
    paymentMethodsAnalytics.send3dsStart(type)
  }

  fun send3dsCancel() {
    paymentMethodsAnalytics.send3dsCancel()
  }

  fun send3dsError(error: String?) {
    paymentMethodsAnalytics.send3dsError(error)
  }
}