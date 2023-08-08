package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.GamesListingResponseItem
import io.reactivex.Single
import retrofit2.http.GET

interface GamesApi {
  @GET("appc/games")
  fun getGamesListing(): Single<GamesListingResponseItem>
}
