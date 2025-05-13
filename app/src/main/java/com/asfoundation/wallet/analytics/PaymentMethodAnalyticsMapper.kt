package com.asfoundation.wallet.analytics

import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.ui.iab.PaymentMethodsAnalytics

class PaymentMethodAnalyticsMapper {
  companion object {
    fun mapPaymentToAnalytics(paymentType: String): String =
      when (paymentType) {
        PaymentType.CARD.name -> PaymentMethodsAnalytics.PAYMENT_METHOD_CC
        PaymentType.PAYPAL.name -> PaymentMethodsAnalytics.PAYMENT_METHOD_PP
        PaymentType.PAYPALV2.name -> PaymentMethodsAnalytics.PAYMENT_METHOD_PP_V2
        PaymentType.GOOGLEPAY_WEB.name -> PaymentMethodsAnalytics.PAYMENT_METHOD_GOOGLEPAY_WEB
        PaymentType.AMAZONPAY.name -> PaymentMethodsAnalytics.PAYMENT_METHOD_AMAZON_PAY
        PaymentType.VKPAY.name -> PaymentMethodsAnalytics.PAYMENT_METHOD_VKPAY
        PaymentType.MI_PAY.name -> PaymentMethodsAnalytics.PAYMENT_METHOD_MI_PAY
        PaymentType.TRUE_LAYER.name -> PaymentMethodsAnalytics.PAYMENT_METHOD_TRUE_LAYER
        PaymentType.LOCAL_PAYMENTS.name -> PaymentMethodsAnalytics.PAYMENT_METHOD_LOCAL
        PaymentType.SANDBOX.name -> PaymentMethodsAnalytics.PAYMENT_METHOD_SANDBOX

        else -> paymentType
      }
  }
}
