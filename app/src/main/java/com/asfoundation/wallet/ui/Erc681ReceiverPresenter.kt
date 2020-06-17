package com.asfoundation.wallet.ui

import android.os.Bundle
import android.util.Log
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.service.AccountWalletService
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.util.TransferParser
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable

internal class Erc681ReceiverPresenter(private val view: Erc681ReceiverView,
                                       private val transferParser: TransferParser,
                                       private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                                       private val walletService: AccountWalletService,
                                       private val data: String,
                                       private val viewScheduler: Scheduler) {
  private var disposable: Disposable? = null
  fun present(savedInstanceState: Bundle?) {
    if (savedInstanceState == null) {
      disposable =
          handleWalletCreationIfNeeded().takeUntil { it != "Creating" }
              .flatMap {
                transferParser.parse(data)
                    .map { transactionBuilder: TransactionBuilder ->
                      var callingPackage = transactionBuilder.domain
                      if (callingPackage == null) {
                        callingPackage = view.callingPackage
                      }
                      transactionBuilder.domain = callingPackage
                      transactionBuilder
                    }
                    .flatMap { transactionBuilder: TransactionBuilder ->
                      inAppPurchaseInteractor.isWalletFromBds(
                          transactionBuilder.domain, transactionBuilder.toAddress())
                          .doOnSuccess { isBds: Boolean? ->
                            view.startEipTransfer(transactionBuilder, isBds,
                                transactionBuilder.payload)
                          }
                    }
                    .toObservable()

              }
              .subscribe({ }, { throwable: Throwable? -> view.startApp(throwable) })
    }
  }

  private fun handleWalletCreationIfNeeded(): Observable<String> {
    return walletService.findWalletOrCreate()
        .observeOn(viewScheduler)
        .doOnNext {
          Log.e("TEST", "FIND WALLET RESULT: $it")
          when (it) {
            "Creating" -> view.showLoadingAnimation()
          }
        }
        .filter { it != "Creating" }
        .map {
          Log.e("TEST", "END ANIMATION: $it")
          view.endAnimation()
          it
        }
  }

  fun pause() {
    if (disposable != null && !disposable!!.isDisposed) {
      disposable!!.dispose()
    }
  }

}