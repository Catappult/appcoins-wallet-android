package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.GamesListingResponseItem
import io.reactivex.Single
import retrofit2.http.GET

interface EskillsGamesApi {

    @GET("get?cdn=web&q=bXlDUFU9YXJtNjQtdjhhLGFybWVhYmktdjdhLGFybWVhYmkmbGVhbmJhY2s9MA&aab=1&mature=false&language=en&not_apk_tags=&offset=0&limit=20&sort=downloads7d&origin=SITE&store_name=apps&group_name=e-skills")
    fun getEskillsGamesListing(): Single<GamesListingResponseItem>
}