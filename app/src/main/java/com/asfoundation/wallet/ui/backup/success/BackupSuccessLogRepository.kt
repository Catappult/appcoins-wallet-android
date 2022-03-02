package com.asfoundation.wallet.ui.backup.success

import com.asfoundation.wallet.base.RxSchedulers
import io.reactivex.Completable
import retrofit2.http.Header
import retrofit2.http.POST
import javax.inject.Inject

class BackupSuccessLogRepository @Inject constructor(private val backupLogApi: BackupLogApi,
                                                     private val rxSchedulers: RxSchedulers) {

  fun logBackupSuccess(ewt: String): Completable {
    return backupLogApi.logBackupSuccess(ewt)
        .subscribeOn(rxSchedulers.io)
  }

  interface BackupLogApi {
    @POST("/transaction/wallet/backup/")
    fun logBackupSuccess(
        @Header("authorization") authorization: String): Completable
  }
}