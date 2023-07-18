package com.asfoundation.wallet.repository

import com.appcoins.wallet.ui.widgets.GameData
import io.reactivex.Single

interface EskillsGamesRepositoryType {

    fun getEskillsGamesListing(): Single<List<GameData>>
}