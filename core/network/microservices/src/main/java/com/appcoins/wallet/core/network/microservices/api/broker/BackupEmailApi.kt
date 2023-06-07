package com.appcoins.wallet.core.network.microservices.api.broker

import com.appcoins.wallet.core.network.microservices.model.EmailBody
import io.reactivex.Completable
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface BackupEmailApi {
  @POST("8.20210201/wallet/backup")
  fun sendBackupEmail(
    @Query("wallet.address") walletAddress: String,
    @Query("wallet.signature") walletSignature: String,
    @Header("authorization") authorization: String,
    @Body emailBody: EmailBody
  ): Completable
}