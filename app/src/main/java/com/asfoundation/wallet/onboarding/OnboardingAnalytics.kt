package com.asfoundation.wallet.onboarding

import cm.aptoide.analytics.AnalyticsManager
import javax.inject.Inject

class OnboardingAnalytics @Inject constructor(private val analyticsManager: AnalyticsManager) {

  companion object {
    private const val WALLET = "WALLET"
    const val WALLET_ONBOARDING_RECOVER_WEB = "wallet_onboarding_recover_web"
    const val BONUS = "bonus"
    const val BONUS_CURRENCY = "bonus_curency"
  }

  fun sendRecoverGuestWalletEvent(bonus: String, bonusCurrency: String) {
    val data = HashMap<String, Any>()
    data[BONUS] = bonus
    data[BONUS_CURRENCY] = bonusCurrency
    analyticsManager.logEvent(
        data, WALLET_ONBOARDING_RECOVER_WEB, AnalyticsManager.Action.CLICK, WALLET)
  }
}
