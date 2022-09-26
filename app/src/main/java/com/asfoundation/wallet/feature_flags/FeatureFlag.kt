package com.asfoundation.wallet.feature_flags

data class FeatureFlag(
  val isNew: Boolean,
  val variant: String,
  val payload: String,
)

interface VariantUseCase<V> {
  suspend fun getVariant(): V?
  suspend fun setImpressed()
}

interface FeatureFlagsRepository {
  suspend fun getFeatureFlag(
    flagId: String,
    userId: String,
    // Profile values should be either Boolean or String or Number. Objects not supported
    profileData: Map<String, Any>
  ): FeatureFlag?

  suspend fun sendImpression(flagId: String, userId: String)
  suspend fun sendAction(flagId: String, userId: String, name: String, payload: String)
}
