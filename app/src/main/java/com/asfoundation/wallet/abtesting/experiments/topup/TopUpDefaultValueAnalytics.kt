package com.asfoundation.wallet.abtesting.experiments.topup

import cm.aptoide.analytics.AnalyticsManager
import java.util.*
import javax.inject.Inject

class TopUpABTestingAnalytics @Inject constructor(private val analyticsManager: AnalyticsManager) {

  private var cachedAssignment = TopUpDefaultValueExperiment.NO_EXPERIMENT

  companion object {
    private const val WALLET = "WALLET"
    const val TOPUP_DEFAULT_VALUE_PARTICIPATING_EVENT =
        "wallet_top_default_value_ab_testing_participating"
  }

  fun sendAbTestParticipatingEvent(group: String) {
    if (group != TopUpDefaultValueExperiment.NO_EXPERIMENT) {
      cachedAssignment = group
      val data = HashMap<String, Any>()
      data["group"] = group
      analyticsManager.logEvent(data, TOPUP_DEFAULT_VALUE_PARTICIPATING_EVENT,
          AnalyticsManager.Action.IMPRESSION, WALLET)
    }
  }
}