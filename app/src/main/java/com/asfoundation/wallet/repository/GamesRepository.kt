package com.asfoundation.wallet.repository

import com.appcoins.wallet.core.network.backend.api.GamesApi
import com.appcoins.wallet.ui.widgets.GameData
import io.reactivex.Single
import java.util.Locale
import javax.inject.Inject

class GamesRepository @Inject constructor(private val gamesApi: GamesApi) :
  GamesRepositoryType {

  override fun getGamesListing(): Single<List<GameData>> {
    return gamesApi.getGamesListing(
      Locale.getDefault().language
    )
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
