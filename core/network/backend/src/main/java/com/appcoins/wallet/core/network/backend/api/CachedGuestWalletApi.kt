package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.CachedGuestWalletResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.GET

interface CachedGuestWalletApi {
  @GET("appc/guest_wallet/cached_values")
  suspend fun getCachedGuestWallet(): Response<CachedGuestWalletResponse>

}
