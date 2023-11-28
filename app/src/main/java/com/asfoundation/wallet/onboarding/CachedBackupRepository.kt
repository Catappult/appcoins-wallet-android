package com.asfoundation.wallet.onboarding

import com.appcoins.wallet.core.network.backend.api.CachedBackupApi
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import io.reactivex.Single
import javax.inject.Inject

class CachedBackupRepository @Inject constructor(
  val api: CachedBackupApi,
  val rxSchedulers: RxSchedulers
) {

  fun getCachedBackup(): Single<String?> {
    return api.getCachedBackup()
      .map { it.backup }
      .subscribeOn(rxSchedulers.io)
      .onErrorReturn {
        null
      }
  }
}
