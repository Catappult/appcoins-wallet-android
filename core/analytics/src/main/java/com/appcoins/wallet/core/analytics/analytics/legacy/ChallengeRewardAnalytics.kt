package com.appcoins.wallet.core.analytics.analytics.legacy

import cm.aptoide.analytics.AnalyticsManager
import javax.inject.Inject

class ChallengeRewardAnalytics @Inject constructor(private val analyticsManager: AnalyticsManager) {

  fun sendChallengeRewardEvent(flowPath: String) {
    val data = mapOf(CHALLENGE_REWARD_FLOW_PATH to flowPath)
    analyticsManager.logEvent(
      data, CHALLENGE_REWARD_EVENT,
      AnalyticsManager.Action.OPEN, CHALLENGE_REWARD
    )
  }

  companion object {
    const val CHALLENGE_REWARD_EVENT = "challenge_reward_event"
    private const val CHALLENGE_REWARD_FLOW_PATH = "challenge_reward_flow_path"
    private const val CHALLENGE_REWARD = "challenge_reward"
  }
}
