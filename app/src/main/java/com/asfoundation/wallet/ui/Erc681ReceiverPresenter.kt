package com.asfoundation.wallet.ui

import android.os.Bundle
import com.appcoins.wallet.bdsbilling.WalletService
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletGetterStatus
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.util.TransferParser
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

internal class Erc681ReceiverPresenter(private val view: Erc681ReceiverView,
                                       private val transferParser: TransferParser,
                                       private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                                       private val walletService: WalletService,
                                       private val data: String,
                                       private val viewScheduler: Scheduler,
                                       private var disposables: CompositeDisposable,
                                       private val productName: String?) {
  fun present(savedInstanceState: Bundle?) {
    if (savedInstanceState == null) {
      disposables.add(
        handleWalletCreationIfNeeded()
          .takeUntil { it != WalletGetterStatus.CREATING.toString() }
          .flatMap {
            transferParser.parse(data)
              .map { transactionBuilder ->
                var callingPackage = transactionBuilder.domain
                if (callingPackage == null) callingPackage = view.getCallingPackage()
                transactionBuilder.domain = callingPackage
                transactionBuilder.productName = productName
                transactionBuilder
              }
              .flatMap { transactionBuilder ->
                inAppPurchaseInteractor.isWalletFromBds(transactionBuilder.domain,
                  transactionBuilder.toAddress())
                  .doOnSuccess { isBds -> view.startEipTransfer(transactionBuilder, isBds) }
              }
              .toObservable()

          }
          .subscribe({ }, { view.startApp(it) })
      )
    }
  }

  private fun handleWalletCreationIfNeeded(): Observable<String> {
    return walletService.findWalletOrCreate()
      .observeOn(viewScheduler)
      .doOnNext {
        if (it == com.appcoins.wallet.feature.walletInfo.data.wallet.WalletGetterStatus.CREATING.toString()) {
          view.showLoadingAnimation()
        }
      }
      .filter { it != com.appcoins.wallet.feature.walletInfo.data.wallet.WalletGetterStatus.CREATING.toString() }
      .map {
        view.endAnimation()
        it
      }
  }

  fun pause() {
    disposables.clear()
  }

}