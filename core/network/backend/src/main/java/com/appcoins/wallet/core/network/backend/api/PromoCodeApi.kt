package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.PromoCodeBonusResponse
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path

interface PromoCodeApi {
  @GET("gamification/perks/promo_code/{promoCodeString}/")
  fun getPromoCodeBonus(
    @Path("promoCodeString") promoCodeString: String
  ): Single<PromoCodeBonusResponse>
}