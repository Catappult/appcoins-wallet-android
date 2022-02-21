package com.asfoundation.wallet.rating

import cm.aptoide.analytics.AnalyticsManager
import java.util.*
import javax.inject.Inject

class RatingAnalytics @Inject constructor(private val analyticsManager: AnalyticsManager) {

  companion object {
    const val WALLET_RATING_WELCOME_EVENT = "wallet_rating_welcome"
    const val WALLET_RATING_POSITIVE_EVENT = "wallet_rating_positive"
    const val WALLET_RATING_NEGATIVE_EVENT = "wallet_rating_negative"
    const val WALLET_RATING_FINISH_EVENT = "wallet_rating_finish"

    private const val ACTION = "action"
    private const val FIRST_TIME = "first_time"
    private const val WALLET_CONTEXT = "wallet"
  }

  fun sendWelcomeActionEvent(action: String) {
    val eventData = HashMap<String, Any>()
    eventData[ACTION] = action

    analyticsManager.logEvent(eventData, WALLET_RATING_WELCOME_EVENT, AnalyticsManager.Action.CLICK,
        WALLET_CONTEXT)
  }

  fun sendPositiveActionEvent(action: String, firstTime: Boolean) {
    val eventData = HashMap<String, Any>()
    eventData[ACTION] = action
    eventData[FIRST_TIME] = firstTime

    analyticsManager.logEvent(eventData, WALLET_RATING_POSITIVE_EVENT,
        AnalyticsManager.Action.CLICK, WALLET_CONTEXT)
  }

  fun sendNegativeActionEvent(action: String) {
    val eventData = HashMap<String, Any>()
    eventData[ACTION] = action

    analyticsManager.logEvent(eventData, WALLET_RATING_NEGATIVE_EVENT,
        AnalyticsManager.Action.CLICK, WALLET_CONTEXT)
  }

  fun sendFinishEvent() {
    analyticsManager.logEvent(HashMap<String, Any>(), WALLET_RATING_FINISH_EVENT,
        AnalyticsManager.Action.CLICK, WALLET_CONTEXT)
  }
}