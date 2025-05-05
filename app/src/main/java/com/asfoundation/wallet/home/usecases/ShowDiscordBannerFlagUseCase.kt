package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.repository.FeatureFlagsRepository
import io.reactivex.Single
import javax.inject.Inject

class ShowDiscordBannerFlagUseCase @Inject constructor(
  private val featureFlagsRepository: FeatureFlagsRepository
) {

  val discordFlag = "k97isy7527ta5roux"
  val flagId = 11

  operator fun invoke(): Single<Boolean> {
    return featureFlagsRepository.getFeatureFlag(discordFlag, flagId)
  }

}
