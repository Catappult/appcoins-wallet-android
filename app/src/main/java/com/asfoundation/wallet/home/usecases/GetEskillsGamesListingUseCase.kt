package com.asfoundation.wallet.home.usecases

import com.appcoins.wallet.ui.widgets.GameData
import com.asfoundation.wallet.repository.EskillsGamesRepositoryType
import io.reactivex.Single
import javax.inject.Inject

class GetEskillsGamesListingUseCase @Inject constructor(
  private val eSkillsGamesRepositoryType: EskillsGamesRepositoryType
) {

  operator fun invoke(): Single<List<GameData>> {
    return eSkillsGamesRepositoryType.getGamesListing()
  }
}
