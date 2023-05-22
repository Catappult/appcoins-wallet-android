package com.appcoins.wallet.core.analytics.analytics.gamification

import cm.aptoide.analytics.AnalyticsManager
import it.czerwinski.android.hilt.annotations.BoundTo
import java.util.*
import javax.inject.Inject

@BoundTo(supertype = GamificationEventSender::class)
class GamificationAnalytics @Inject constructor(private val analytics: AnalyticsManager) :
    GamificationEventSender {

  companion object {
    const val GAMIFICATION = "GAMIFICATION"
    const val GAMIFICATION_MORE_INFO = "GAMIFICATION_MORE_INFO"
    private const val EVENT_USER_LEVEL = "user_level"
    private const val WALLET = "WALLET"

  }

  override fun sendMainScreenViewEvent(userLevel: Int) {
    val eventData = HashMap<String, Any>()
    eventData[EVENT_USER_LEVEL] = userLevel

    analytics.logEvent(eventData, GAMIFICATION, AnalyticsManager.Action.VIEW, WALLET)
  }

  override fun sendMoreInfoScreenViewEvent(userLevel: Int) {
    val eventData = HashMap<String, Any>()
    eventData[EVENT_USER_LEVEL] = userLevel

    analytics.logEvent(eventData, GAMIFICATION_MORE_INFO, AnalyticsManager.Action.VIEW, WALLET)
  }
}