package com.appcoins.wallet.core.network.eskills.api

import com.appcoins.wallet.core.network.eskills.model.GamesListingResponseItem
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface EskillsGamesApi {

    //@GET("appc/games")
    @GET("apps/get")
    fun getGamesListing(
        @Query("limit") limit:String,
        @Query("sort") sort:String,
        @Query("store_name") store:String,
        @Query("group_name") group:String,
        @Query("language") language:String
    ): Single<GamesListingResponseItem>
}