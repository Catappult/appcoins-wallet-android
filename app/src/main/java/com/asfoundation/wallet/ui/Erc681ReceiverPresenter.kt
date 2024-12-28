package com.asfoundation.wallet.ui

import android.os.Bundle
import com.appcoins.wallet.core.analytics.analytics.partners.PartnerAddressService
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletGetterStatus
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.webview_payment.usecases.CreateWebViewPaymentOspUseCase
import com.asfoundation.wallet.ui.webview_payment.usecases.CreateWebViewPaymentSdkUseCase
import com.asfoundation.wallet.ui.webview_payment.usecases.IsWebViewPaymentFlowUseCase
import com.asfoundation.wallet.util.TransferParser
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable

internal class Erc681ReceiverPresenter(
  private val view: Erc681ReceiverView,
  private val transferParser: TransferParser,
  private val inAppPurchaseInteractor: InAppPurchaseInteractor,
  private val walletService: WalletService,
  private val data: String,
  private val viewScheduler: Scheduler,
  private var disposables: CompositeDisposable,
  private val productName: String?,
  private val partnerAddressService: PartnerAddressService,
  private val createWebViewPaymentSdkUseCase: CreateWebViewPaymentSdkUseCase,
  private val isWebViewPaymentFlowUseCase: IsWebViewPaymentFlowUseCase,
  private val rxSchedulers: RxSchedulers,
) {
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
                partnerAddressService.setOemIdFromSdk(transactionBuilder.oemIdSdk)
                Single.zip(
                  isWebViewPaymentFlowUseCase(transactionBuilder).subscribeOn(rxSchedulers.io),
                  inAppPurchaseInteractor.isWalletFromBds(
                    transactionBuilder.domain,
                    transactionBuilder.toAddress()
                  )
                    .subscribeOn(rxSchedulers.io),
                ) { isWebPaymentFlow, isBds ->
                  Pair(isWebPaymentFlow, isBds)
                }
                  .map {
                    val isWebPaymentFlow = it.first
                    val isBds = it.second
                    if (isWebPaymentFlow.paymentMethods?.walletWebViewPayment != null) {
                      startWebViewPayment(transactionBuilder)
                    } else {
                      view.startEipTransfer(transactionBuilder, isBds)
                      Observable.just("")
                    }
                  }
              }
              .toObservable()

          }
          // TODO this onError seems like a mistake. we need to investigate it further:
          .subscribe({ }, { view.startApp(it) })
      )
    }
  }

  private fun startWebViewPayment(
    transaction: TransactionBuilder,
  ): Single<String> {
    return createWebViewPaymentSdkUseCase(transaction)
      .doOnSuccess { url ->
        view.launchWebViewPayment(url, transaction)
      }
  }

  private fun handleWalletCreationIfNeeded(): Observable<String> {
    return walletService.findWalletOrCreate()
      .observeOn(viewScheduler)
      .doOnNext {
        if (it == WalletGetterStatus.CREATING.toString()) {
          view.showLoadingAnimation()
        }
      }
      .filter { it != WalletGetterStatus.CREATING.toString() }
      .map {
        view.endAnimation()
        it
      }
  }

  fun pause() {
    disposables.clear()
  }

}