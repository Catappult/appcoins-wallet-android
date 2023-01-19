package com.asfoundation.wallet.onboarding_new_payment

import com.appcoins.wallet.billing.adyen.AdyenPaymentRepository
import com.asfoundation.wallet.billing.adyen.PaymentType
import com.asfoundation.wallet.billing.analytics.BillingAnalytics

fun PaymentType.mapToAnalytics(): String =
  if (this.name == PaymentType.CARD.name) {
    BillingAnalytics.PAYMENT_METHOD_CC
  } else {
    BillingAnalytics.PAYMENT_METHOD_PAYPAL
  }

fun PaymentType.mapToService(): AdyenPaymentRepository.Methods =
  if (this.name == PaymentType.CARD.name) {
    AdyenPaymentRepository.Methods.CREDIT_CARD
  } else {
    AdyenPaymentRepository.Methods.PAYPAL
  }
