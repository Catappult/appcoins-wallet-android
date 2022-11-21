package com.asfoundation.wallet.billing.paypal

import android.util.Log
import com.appcoins.wallet.bdsbilling.WalletService
import com.asf.wallet.BuildConfig
import com.asfoundation.wallet.billing.PayPalV2Repository
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class CancelPaypalTokenUseCase @Inject constructor(
  private val walletService: WalletService,
  private val payPalV2Repository: PayPalV2Repository
) {

  operator fun invoke(token: String): Completable {
    return walletService.getAndSignCurrentWalletAddress()
      .subscribeOn(Schedulers.io())
      .flatMapCompletable { addressModel ->
        Log.d(this.toString(), "Canceling token")
        payPalV2Repository.cancelToken(
          walletAddress = addressModel.address,
          walletSignature = addressModel.signedAddress,
          token = token
        )
      }
      .onErrorComplete {
        Log.d(this.toString(), it.message ?: "")
        true
      }
  }

}
