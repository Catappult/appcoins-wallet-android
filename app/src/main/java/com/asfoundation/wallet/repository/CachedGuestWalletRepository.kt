package com.asfoundation.wallet.repository

import com.appcoins.wallet.core.network.backend.api.CachedGuestWalletApi
import com.asfoundation.wallet.onboarding.BackupModel
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxSingle
import javax.inject.Inject

class CachedGuestWalletRepository @Inject constructor(
  private val api: CachedGuestWalletApi,
) {

  fun getCachedGuestWallet(): Single<BackupModel?> {
    return rxSingle {
      val response = api.getCachedGuestWallet()
      with(response) {
        if (code() == 204)
          BackupModel("", "")
        else
          BackupModel(body()?.privateKey ?: "", body()?.flow ?: "")
      }
    }.onErrorReturn { null }
  }

  fun deleteCachedGuestWallet(ewt: String): Completable {
    return api.deleteCachedGuestWallet(ewt)
      .onErrorComplete()
  }

}
