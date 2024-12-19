package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.CachedGuestWalletResponse
import io.reactivex.Completable
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET

interface CachedGuestWalletApi {
  @GET("appc/guest_wallet/cached_values")
  suspend fun getCachedGuestWallet(): Response<CachedGuestWalletResponse>

  @DELETE("appc/guest_wallet")
  fun deleteCachedGuestWallet(): Completable

}
