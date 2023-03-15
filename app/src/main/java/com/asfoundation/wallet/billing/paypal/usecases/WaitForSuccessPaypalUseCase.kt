package com.asfoundation.wallet.billing.paypal.usecases

import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.billing.adyen.PaymentModel
import com.appcoins.wallet.core.utils.common.RxSchedulers
import com.asfoundation.wallet.billing.paypal.repository.PayPalV2Repository
import io.reactivex.Observable
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class WaitForSuccessPaypalUseCase @Inject constructor(
  private val walletService: WalletService,
  private val payPalV2Repository: PayPalV2Repository,
  private val rxSchedulers: RxSchedulers,

  ) {
  operator fun invoke(uid: String): Observable<PaymentModel> {
    var lastPaymentCheck: PaymentModel? = null
    return walletService.getAndSignCurrentWalletAddress()
      .flatMapObservable { walletAddressModel ->
        Observable.interval(0, 5, TimeUnit.SECONDS, rxSchedulers.io)
          .timeInterval()
          .switchMap {
            if(!isEndingState(lastPaymentCheck?.status))
              payPalV2Repository.getTransaction(uid, walletAddressModel.address,
                walletAddressModel.signedAddress)
                .doOnSuccess { lastPaymentCheck = it }
                .toObservable()
            else
              Observable.just(lastPaymentCheck)
          }
          .filter {
            isEndingState(it.status)
          }
          .distinctUntilChanged { transaction ->
            transaction.status
          }
      }
  }

  private fun isEndingState(status: PaymentModel.Status?): Boolean {
    return (status == PaymentModel.Status.COMPLETED
        || status == PaymentModel.Status.FAILED
        || status == PaymentModel.Status.CANCELED
        || status == PaymentModel.Status.INVALID_TRANSACTION
        || status == PaymentModel.Status.FRAUD)
  }

  private fun isCompleted(status: PaymentModel.Status?): Boolean {
    return status == PaymentModel.Status.COMPLETED
  }

  private fun isFail(status: PaymentModel.Status?): Boolean {
    return (status == PaymentModel.Status.FAILED
        || status == PaymentModel.Status.CANCELED
        || status == PaymentModel.Status.INVALID_TRANSACTION
        || status == PaymentModel.Status.FRAUD )
  }

}
