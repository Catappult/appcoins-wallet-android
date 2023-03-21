package com.appcoins.wallet.core.network.backend.api

import io.reactivex.Completable
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface RedeemGiftApi {
  @POST("gamification/giftcard/{giftcard_key}/redeem")
  fun redeemGiftCode(
    @Path("giftcard_key") giftCode: String,
    @Header("authorization") authorization: String
  ): Completable
}