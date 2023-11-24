package com.asfoundation.wallet.onboarding

import cm.aptoide.analytics.AnalyticsManager
import java.util.*
import javax.inject.Inject

class OnboardingAnalytics @Inject constructor(private val analyticsManager: AnalyticsManager) {

  companion object {
    private const val WALLET = "WALLET"
    const val WALLET_ONBOARDING_RECOVER_WEB = "wallet_onboarding_recover_web"
  }

  fun sendRecoverGuestWalletEvent(bonus: String, bonusCurrency: String) {
    val data = HashMap<String, Any>()
    data["bonus"] = bonus
    data["bonus_curency"] = bonusCurrency
    analyticsManager.logEvent(data, WALLET_ONBOARDING_RECOVER_WEB, AnalyticsManager.Action.CLICK, WALLET)
  }
}