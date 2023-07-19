package com.asfoundation.wallet.repository

import com.appcoins.wallet.ui.widgets.GameData
import com.appcoins.wallet.ui.widgets.GameDetailsData
import io.reactivex.Single

interface GamesRepositoryType {

  fun getGamesListing(): Single<List<GameData>>

  fun getGameDetails(packageName: String): Single<GameDetailsData>

}
