package com.asfoundation.wallet.home.usecases

import com.appcoins.wallet.ui.widgets.GameData
import com.appcoins.wallet.ui.widgets.GameDetailsData
import com.asfoundation.wallet.repository.GamesRepositoryType
import io.reactivex.Single
import javax.inject.Inject

class GetGameDetailsUseCase @Inject constructor(
    private val gamesRepositoryType: GamesRepositoryType
) {

    operator fun invoke(gamePackage: String): Single<GameDetailsData> {
        return gamesRepositoryType.getGameDetails(gamePackage)
    }
}