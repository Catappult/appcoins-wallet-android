package com.asfoundation.wallet.feature_flags.topup

import cm.aptoide.analytics.AnalyticsManager
import com.asfoundation.wallet.feature_flags.FeatureFlag
import com.asfoundation.wallet.feature_flags.FeatureFlagsRepository

class TopUpDefaultValueProbe constructor(
  private val featureFlagsRepository: FeatureFlagsRepository,
  private val analyticsManager: AnalyticsManager
) : FeatureFlagsRepository {

  private var featureFlag: FeatureFlag? = null

  override suspend fun getFeatureFlag(
    flagId: String,
    userId: String,
    profileData: Map<String, Any>
  ): FeatureFlag? = featureFlagsRepository
    .getFeatureFlag(flagId, userId, profileData)
    .also { featureFlag = it }

  override suspend fun sendImpression(flagId: String, userId: String) {
    featureFlag?.run {
      analyticsManager.logEvent(
        mapOf("group" to variant),
        TOPUP_DEFAULT_VALUE_PARTICIPATING_EVENT,
        AnalyticsManager.Action.IMPRESSION,
        WALLET
      )
    }
    featureFlagsRepository.sendImpression(flagId, userId)
  }

  override suspend fun sendAction(
    flagId: String,
    userId: String,
    name: String,
    payload: String
  ) = featureFlagsRepository.sendAction(flagId, userId, name, payload)

  companion object {
    private const val WALLET = "WALLET"
    const val TOPUP_DEFAULT_VALUE_PARTICIPATING_EVENT =
      "wallet_top_default_value_ab_testing_participating"
  }
}