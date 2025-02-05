package com.asfoundation.wallet.repository

import com.appcoins.wallet.core.network.flagr.api.FlagrNetworkApi
import com.appcoins.wallet.core.network.flagr.model.EmptyContext
import com.appcoins.wallet.core.network.flagr.model.FlagrRequest
import io.reactivex.Single
import javax.inject.Inject

class FeatureFlagsRepository @Inject constructor(
  private val api: FlagrNetworkApi,
) {

  fun getFeatureFlag(flagKey: String, flagId: Int): Single<Boolean> {
    return api.getFeatureFlag(
      FlagrRequest(
        entityID = "",
        entityType = "user",
        entityContext = EmptyContext(),
        enableDebug = true,
        flagID = flagId,
        flagKey = flagKey,
        flagTags = listOf(),
        flagTagsOperator = "ANY"
      )
    )
      .map { it.variantKey == "active" }
  }

}
