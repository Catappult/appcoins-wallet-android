package com.asfoundation.wallet.ui.backup.success

import android.util.Log
import com.asfoundation.wallet.base.RxSchedulers
import io.reactivex.Completable
import retrofit2.http.Header
import retrofit2.http.POST

class BackupSuccessLogRepository(private val backupLogApi: BackupLopApi,
                                 private val rxSchedulers: RxSchedulers) {

  fun logBackupSuccess(ewt: String): Completable {
    return backupLogApi.logBackupSuccess(ewt)
        .subscribeOn(rxSchedulers.io)
  }

  interface BackupLopApi {
    @POST("/transaction/wallet/backup/")
    fun logBackupSuccess(
        @Header("authorization") authorization: String): Completable
  }
}