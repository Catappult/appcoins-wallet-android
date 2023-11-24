package com.asfoundation.wallet.repository

import android.util.Log
import com.appcoins.wallet.core.network.backend.api.CachedGuestWalletApi
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.rx2.rxSingle
import okhttp3.internal.wait
import javax.inject.Inject

class CachedGuestWalletRepository @Inject constructor(
  private val api: CachedGuestWalletApi,
  private val rxSchedulers: RxSchedulers,
) {

  fun getCachedGuestWallet(): Single<String?> {
    return rxSingle {
      val response = api.getCachedGuestWallet()
      if (response.code() == 204)
        ""
      else
        response.body()?.privateKey ?: ""
    }
      .onErrorReturn { null }
  }

  fun deleteCachedGuestWallet(ewt: String): Completable {
    return api.deleteCachedGuestWallet(ewt)
      .onErrorComplete()
  }

}
