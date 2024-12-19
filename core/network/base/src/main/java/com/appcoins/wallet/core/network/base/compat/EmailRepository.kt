package com.appcoins.wallet.core.network.base.compat

import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import io.reactivex.Completable
import javax.inject.Inject

class EmailRepository @Inject constructor(
  private val emailApi: EmailApi,
  private val rxSchedulers: RxSchedulers
) {

  fun postUserEmail(email: String): Completable {
    return emailApi.postUserEmail(WalletEmailRequest(email)).subscribeOn(rxSchedulers.io)
  }

}
