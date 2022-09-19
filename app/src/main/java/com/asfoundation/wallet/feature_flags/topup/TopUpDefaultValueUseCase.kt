package com.asfoundation.wallet.feature_flags.topup

import com.asfoundation.wallet.feature_flags.FeatureFlagsRepository
import com.asfoundation.wallet.feature_flags.VariantUseCase
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class TopUpDefaultValueUseCase constructor(
  private val featureFlagsRepository: FeatureFlagsRepository,
  private val androidIdRepository: AndroidIdRepository,
  private val ioDispatcher: CoroutineDispatcher
) : VariantUseCase<Int> {

  private val mutex = Mutex()
  private val userId by lazy(androidIdRepository::getAndroidId)
  private var value: Int? = null
  private var cached: Boolean = false

  override suspend fun getVariant(): Int? = withContext(ioDispatcher) {
    mutex.withLock {
      if (cached) {
        value
      } else {
        value = featureFlagsRepository
          .getFeatureFlag(FEATURE_FLAG_ID, userId, emptyMap())
          ?.payload
          ?.toIntOrNull()
        cached = true
        value
      }
    }
  }

  override suspend fun setImpressed() = withContext(ioDispatcher) {
    featureFlagsRepository.sendImpression(FEATURE_FLAG_ID, userId)
  }

  suspend fun setTopUpWith(appcValue: Double) = withContext(ioDispatcher) {
    featureFlagsRepository.sendAction(
      flagId = FEATURE_FLAG_ID,
      userId = userId,
      name = TOP_UP_EVENT,
      payload = appcValue.toString()
    )
  }

  companion object {
    internal const val TOP_UP_EVENT = "TOP_UP"
    internal const val FEATURE_FLAG_ID = "APPC-2448-topup-default-value"
  }
}

interface AndroidIdRepository {
  fun getAndroidId(): String
}
