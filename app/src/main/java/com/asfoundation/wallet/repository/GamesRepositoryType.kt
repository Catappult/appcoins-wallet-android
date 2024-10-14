package com.asfoundation.wallet.repository

import com.appcoins.wallet.ui.widgets.GameData
import io.reactivex.Single

interface GamesRepositoryType {

  fun getGamesListing(): Single<List<GameData>>

}
