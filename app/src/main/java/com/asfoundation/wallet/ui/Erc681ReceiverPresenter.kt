package com.asfoundation.wallet.ui

import android.os.Bundle
import android.util.Log
import com.appcoins.wallet.core.analytics.analytics.legacy.BillingAnalytics
import com.appcoins.wallet.core.analytics.analytics.partners.AddressService
import com.appcoins.wallet.core.analytics.analytics.partners.PartnerAddressService
import com.appcoins.wallet.core.utils.android_common.RxSchedulers
import com.appcoins.wallet.core.utils.jvm_common.Logger
import com.appcoins.wallet.core.walletservices.WalletService
import com.appcoins.wallet.feature.walletInfo.data.wallet.WalletGetterStatus
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.InAppPurchaseInteractor
import com.asfoundation.wallet.ui.webview_payment.usecases.CreateWebViewPaymentSdkUseCase
import com.asfoundation.wallet.ui.webview_payment.usecases.IsWebViewPaymentFlowUseCase
import com.asfoundation.wallet.util.TransferParser
import io.reactivex.Completable
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
  private val billingAnalytics: BillingAnalytics,
  private var addressService: AddressService,
  private val logger: Logger,
  private val appVersionCode: Int?
) {
  private var firstImpression = true
  private val TAG = this::class.java.simpleName
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
                  isWebViewPaymentFlowUseCase(transactionBuilder, appVersionCode).subscribeOn(
                    rxSchedulers.io
                  ),
                  inAppPurchaseInteractor.isWalletFromBds(
                    transactionBuilder.domain,
                    transactionBuilder.toAddress()
                  )
                    .subscribeOn(rxSchedulers.io),
                ) { isWebPaymentFlow, isBds ->
                  Pair(isWebPaymentFlow, isBds)
                }
                  .flatMap {
                    val isWebPaymentFlow = it.first
                    val isBds = it.second
                    if (
                      isWebPaymentFlow.paymentMethods?.walletWebViewPayment != null &&
                      !transactionBuilder.type.equals("INAPP_SUBSCRIPTION", ignoreCase = true)
                    ) {
                      handlePurchaseStartAnalytics(transactionBuilder)
                      startWebViewPayment(transactionBuilder)
                    } else {
                      view.startEipTransfer(transactionBuilder, isBds)
                      Single.just("")
                    }
                  }
              }
              .toObservable()

          }
          // TODO this onError seems like a mistake. we need to investigate it further:
          .subscribe({ }, {
            Log.i(TAG, "Error in Erc681ReceiverPresenter: ${it.message}")
            logger.log("Erc681ReceiverPresenter", it)
            view.startApp(it)
          })
      )
    }
  }

  private fun startWebViewPayment(
    transaction: TransactionBuilder,
  ): Single<String> {
    return createWebViewPaymentSdkUseCase(transaction, appVersionCode.toString())
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

  private fun handlePurchaseStartAnalytics(transaction: TransactionBuilder?) {
    disposables.add(
      addressService.getAttribution(transaction?.domain ?: "")
        .flatMapCompletable { attribution ->
          Completable.fromAction {
            if (firstImpression) {
              billingAnalytics.sendPurchaseStartEvent(
                packageName = transaction?.domain,
                skuDetails = transaction?.skuId,
                value = transaction?.amount().toString(),
                transactionType = transaction?.type,
                context = BillingAnalytics.WALLET_PAYMENT_METHOD,
                oemId = attribution.oemId,
                isWebViewPayment = true,
              )
              firstImpression = false
            }
          }
        }
        .subscribeOn(rxSchedulers.io)
        .subscribe({}, { it.printStackTrace() })
    )
  }

}