package com.appcoins.wallet.core.network.backend.api

import io.reactivex.Completable
import retrofit2.http.Header
import retrofit2.http.POST

interface BackupLogApi {
  @POST("/transaction/wallet/backup/")
  fun logBackupSuccess(
    @Header("authorization") authorization: String
  ): Completable
}