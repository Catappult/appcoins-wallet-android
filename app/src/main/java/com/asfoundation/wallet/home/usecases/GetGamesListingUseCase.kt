package com.asfoundation.wallet.home.usecases

import com.asfoundation.wallet.repository.GamesRepositoryType
import com.appcoins.wallet.ui.widgets.GameData
import io.reactivex.Single
import javax.inject.Inject

class GetGamesListingUseCase @Inject constructor(
  private val gamesRepositoryType: GamesRepositoryType
) {

  operator fun invoke(): Single<List<GameData>> {
    return gamesRepositoryType.getGamesListing()
  }

}
