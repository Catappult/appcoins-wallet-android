package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.repository.FeatureFlagsRepository
import io.reactivex.Single
import javax.inject.Inject

class ShowRebrandingBannerFlagUseCase @Inject constructor(
  private val featureFlagsRepository: FeatureFlagsRepository
) {

  val rebrandingFlag = "kv35ms9qmawk6nw19"

  operator fun invoke(): Single<Boolean> {
    return featureFlagsRepository.getFeatureFlag(rebrandingFlag)
  }

}
