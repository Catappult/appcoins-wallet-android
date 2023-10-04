package com.asfoundation.wallet.home.usecases

import com.appcoins.wallet.ui.widgets.GameDetailsData
import com.asfoundation.wallet.repository.EskillsGamesRepositoryType
import io.reactivex.Single
import javax.inject.Inject

class GetEskillsGameDetailsUseCase @Inject constructor(
    private val eSkillsGamesRepositoryType: EskillsGamesRepositoryType
) {

    operator fun invoke(gamePackage: String): Single<GameDetailsData> {
        return eSkillsGamesRepositoryType.getGameDetails(gamePackage)
    }
}