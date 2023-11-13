package com.appcoins.wallet.core.network.backend.api

import com.appcoins.wallet.core.network.backend.model.CachedBackupResponse
import com.appcoins.wallet.core.network.backend.model.CachedTransactionResponse
import io.reactivex.Single
import retrofit2.http.GET

interface CachedBackupApi {

  @GET("/appc/guest_wallet/cached_values")
  fun getCachedBackup(): Single<CachedBackupResponse>



}
