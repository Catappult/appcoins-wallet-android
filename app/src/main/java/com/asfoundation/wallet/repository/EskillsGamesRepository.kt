package com.asfoundation.wallet.repository

import com.appcoins.wallet.core.network.backend.api.EskillsGamesApi
import com.appcoins.wallet.ui.widgets.GameData
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = EskillsGamesRepositoryType::class)
class EskillsGamesRepository @Inject constructor(private val gamesApi: EskillsGamesApi) :
    EskillsGamesRepositoryType {

    override fun getEskillsGamesListing(): Single<List<GameData>> {
        return gamesApi.getEskillsGamesListing()
            .map { it.dataList.list.map {
                GameData(
                    title = it.appName,
                    gameIcon = it.appIcon,
                    gameBackground = it.background,
                    gamePackage = it.packageName
                )

            }

            }
    }
}

