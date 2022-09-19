package com.asfoundation.wallet.feature_flags

import com.asfoundation.wallet.feature_flags.api.*
import com.asfoundation.wallet.feature_flags.db.DBFeatureFlag
import com.asfoundation.wallet.feature_flags.db.FeatureFlagsDao
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = FeatureFlagsRepository::class)
class FeatureFlagsRepositoryImpl @Inject constructor(
  private val api: ExperimentsApi,
  private val featureFlagsDao: FeatureFlagsDao
) : FeatureFlagsRepository {

  override suspend fun getFeatureFlag(
    flagId: String,
    userId: String,
    profileData: Map<String, Any>
  ): FeatureFlag? {
    return try {
      api.assignExperiment(
        experimentId = flagId,
        userId = userId,
        body = UserProfile(profileData)
      ).toFeatureFlag()
        .also {
          it?.run { featureFlagsDao.save(toDBFeatureFlag(flagId)) }
            ?: featureFlagsDao.remove(DBFeatureFlag(flagId, "", ""))
        }
    } catch (_: Throwable) {
      featureFlagsDao.get(flagId)?.toFeatureFlag()
    }
  }

  override suspend fun sendImpression(flagId: String, userId: String) =
    try {
      api.sendEvent(
        flagId,
        userId,
        Events(events = listOf(Event(name = IMPRESSION_EVENT)))
      )
    } catch (_: Throwable) {
    }

  override suspend fun sendAction(
    flagId: String,
    userId: String,
    name: String,
    payload: String
  ) {
    require(name != IMPRESSION_EVENT) { "$IMPRESSION_EVENT is reserved. Use recordImpression() for impressions" }
    try {
      api.sendEvent(
        flagId,
        userId,
        Events(events = listOf(element = Event(name = name, payload = payload)))
      )
    } catch (_: Throwable) {
    }
  }

  private fun DBFeatureFlag.toFeatureFlag(): FeatureFlag =
    FeatureFlag(isNew = false, variant = variant, payload = payload)

  private fun FeatureFlag.toDBFeatureFlag(flagId: String): DBFeatureFlag =
    DBFeatureFlag(flagId = flagId, variant = variant, payload = payload)

  private fun ApiAssignment.toFeatureFlag(): FeatureFlag? = when (status) {
    Status.EXPERIMENT_NOT_FOUND -> null
    Status.EXPERIMENT_NOT_STARTED -> null
    Status.EXPERIMENT_IN_DRAFT_STATE -> null
    Status.EXPERIMENT_PAUSED -> null
    Status.NO_PROFILE_MATCH -> null
    Status.EXPERIMENT_EXPIRED -> null
    Status.ASSIGNMENT_FAILED -> null
    Status.NEW_ASSIGNMENT -> FeatureFlag(isNew = true, variant = assignment, payload = payload)
    Status.EXISTING_ASSIGNMENT -> FeatureFlag(
      isNew = false,
      variant = assignment,
      payload = payload
    )
    Status.NO_OPEN_BUCKETS -> null
  }

  companion object {
    const val IMPRESSION_EVENT = "IMPRESSION"
  }
}
