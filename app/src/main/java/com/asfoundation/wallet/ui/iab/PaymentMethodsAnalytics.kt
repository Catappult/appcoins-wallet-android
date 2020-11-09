package com.asfoundation.wallet.ui.iab

import com.asfoundation.wallet.analytics.AmplitudeAnalytics
import com.asfoundation.wallet.analytics.RakamAnalytics
import com.asfoundation.wallet.billing.analytics.BillingAnalytics

class PaymentMethodsAnalytics(private val billingAnalytics: BillingAnalytics,
                              private val rakamAnalytics: RakamAnalytics,
                              private val amplitudeAnalytics: AmplitudeAnalytics) {

  fun setGamificationLevel(cachedGamificationLevel: Int) {
    rakamAnalytics.setGamificationLevel(cachedGamificationLevel)
    amplitudeAnalytics.setGamificationLevel(cachedGamificationLevel)
  }

  fun sendPurchaseDetailsEvent(appPackage: String, skuId: String?, amount: String,
                               type: String?) {
    billingAnalytics.sendPurchaseDetailsEvent(appPackage, skuId, amount, type)
  }

  fun sendPaymentMethodEvent(appPackage: String, skuId: String?, amount: String,
                             paymentId: String, type: String?, action: String,
                             isPreselected: Boolean = false) {
    if (isPreselected) {
      billingAnalytics.sendPreSelectedPaymentMethodEvent(appPackage, skuId, amount, paymentId, type,
          action)
    } else {
      billingAnalytics.sendPaymentMethodEvent(appPackage, skuId, amount, paymentId, type, action)

    }
  }
}
