package com.asfoundation.wallet.repository

import com.appcoins.wallet.core.network.backend.api.GamesApi
import com.appcoins.wallet.ui.widgets.GameData
import io.reactivex.Single
import it.czerwinski.android.hilt.annotations.BoundTo
import javax.inject.Inject

@BoundTo(supertype = GamesRepositoryType::class)
class GamesRepository @Inject constructor(private val gamesApi: GamesApi) :
  GamesRepositoryType {

  override fun getGamesListing(): Single<List<GameData>> {
    return gamesApi.getGamesListing()
      .map {
        it.map {
          GameData(
            title = it.appName,
            gameIcon = it.appIcon,
            gameBackground = it.background,
            gamePackage = it.packageName,
            actionUrl = it.actionUrl
          )
        }
      }
  }

}
