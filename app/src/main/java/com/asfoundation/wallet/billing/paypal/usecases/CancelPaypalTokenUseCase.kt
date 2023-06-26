package com.asfoundation.wallet.billing.paypal.usecases

import android.util.Log
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.asfoundation.wallet.billing.paypal.repository.PayPalV2Repository
import io.reactivex.Completable
import javax.inject.Inject

class CancelPaypalTokenUseCase @Inject constructor(
  private val walletService: WalletService,
  private val payPalV2Repository: PayPalV2Repository,
  private val rxSchedulers: RxSchedulers
) {

  operator fun invoke(token: String): Completable {
    return walletService.getWalletAddress()
      .subscribeOn(rxSchedulers.io)
      .flatMapCompletable { address ->
        Log.d(this.toString(), "Canceling token")
        payPalV2Repository.cancelToken(
          walletAddress = address,
          token = token
        )
      }
      .onErrorComplete {
        Log.d(this.toString(), it.message ?: "")
        true
      }
  }

}
