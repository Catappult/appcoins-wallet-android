package com.asfoundation.wallet.billing.adyen

import com.asfoundation.wallet.billing.analytics.BillingAnalytics

class AdyenPaymentAnalytics(private val analytics: BillingAnalytics) {

  fun sendAnalytics(domain: String, skuId: String?, appcAmount: String, paymentType: String,
                    transactionType: String, action: String?, refusalCode: String?,
                    refusalReason: String?, riskRules: String?, event: Event) {
    when (event) {
      Event.CONFIRMATION -> {
        analytics.sendPaymentConfirmationEvent(domain, skuId, appcAmount,
            mapPaymentToAnalytics(paymentType), transactionType, action)
      }
      Event.PRE_SELECTED_PAYMENT_METHOD -> {
        analytics.sendPreSelectedPaymentMethodEvent(domain, skuId, appcAmount,
            mapPaymentToAnalytics(paymentType), transactionType, action)
      }
      Event.ADYEN_ERROR_AND_RISK_RULES -> {
        analytics.sendPaymentErrorWithDetailsAndRiskEvent(domain, skuId, appcAmount,
            mapPaymentToAnalytics(paymentType), transactionType,
            refusalCode.toString(), refusalReason, riskRules)
      }
      Event.SUCCESS -> {
        analytics.sendPaymentSuccessEvent(domain, skuId, appcAmount,
            mapPaymentToAnalytics(paymentType), transactionType)
      }
      Event.REVENUE -> analytics.sendRevenueEvent(appcAmount)
      Event.PAYMENT_EVENT -> {
        analytics.sendPaymentEvent(domain, skuId, appcAmount,
            mapPaymentToAnalytics(paymentType), transactionType)
      }
      Event.PAYMENT_DETAILS -> {
        analytics.sendPaymentMethodDetailsEvent(domain, skuId, appcAmount,
            mapPaymentToAnalytics(paymentType), transactionType)
      }
    }
  }

  private fun mapPaymentToAnalytics(paymentType: String): String {
    return if (paymentType == PaymentType.CARD.name) {
      BillingAnalytics.PAYMENT_METHOD_CC
    } else {
      BillingAnalytics.PAYMENT_METHOD_PAYPAL
    }
  }

  enum class Event {
    PAYMENT_EVENT, PAYMENT_DETAILS, CONFIRMATION, SUCCESS, REVENUE, PRE_SELECTED_PAYMENT_METHOD,
    ADYEN_ERROR_AND_RISK_RULES
  }
}
