package com.asfoundation.wallet.repository

import com.appcoins.wallet.core.network.backend.api.ImpressionApi
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import io.reactivex.Completable
import javax.inject.Inject

class ImpressionRepository @Inject constructor(
  private val impressionApi: ImpressionApi,
  private val rxSchedulers: RxSchedulers
) {
  
  fun getImpression(): Completable {
    return impressionApi.getImpression().subscribeOn(rxSchedulers.io)
  }

}