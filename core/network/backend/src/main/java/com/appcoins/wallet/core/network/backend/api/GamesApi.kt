package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.GamesListingResponseItem
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface GamesApi {
  @GET("appc/games")
  fun getGamesListing(
    @Query("lang_code") langCode: String,
  ): Single<List<GamesListingResponseItem>>
}
