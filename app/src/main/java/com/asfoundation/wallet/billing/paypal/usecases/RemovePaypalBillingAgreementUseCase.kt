package com.asfoundation.wallet.billing.paypal.usecases

import android.util.Log
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.asfoundation.wallet.billing.paypal.repository.PayPalV2Repository
import io.reactivex.Completable
import javax.inject.Inject

class RemovePaypalBillingAgreementUseCase @Inject constructor(
  private val walletService: WalletService,
  private val payPalV2Repository: PayPalV2Repository,
  private val rxSchedulers: RxSchedulers
) {

  operator fun invoke(): Completable {
    return walletService.getAndSignCurrentWalletAddress()
      .subscribeOn(rxSchedulers.io)
      .flatMapCompletable { addressModel ->
        Log.d(this.toString(), "Removing Agreement")
        payPalV2Repository.removeBillingAgreement(
          walletAddress = addressModel.address,
          walletSignature = addressModel.signedAddress
        )
      }
      .onErrorComplete {// TODO log error
        Log.d(this.toString(), it.message ?: "")
        true
      }
  }

}
