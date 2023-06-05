package com.asfoundation.wallet.ui.transact

import android.content.Intent
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository.Status
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.appcoins.wallet.core.utils.android_common.extensions.isNoNetworkException
import com.appcoins.wallet.feature.walletInfo.data.wallet.usecases.GetWalletInfoUseCase
import com.asfoundation.wallet.ui.barcode.BarcodeCaptureActivity
import com.asfoundation.wallet.ui.transact.TransferFragmentView.Currency
import com.asfoundation.wallet.ui.transact.TransferFragmentView.TransferData
import com.asfoundation.wallet.util.QRUri
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.vision.barcode.Barcode
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class TransferFragmentPresenter(private val view: TransferFragmentView,
                                private val disposables: CompositeDisposable,
                                private val onResumeDisposables: CompositeDisposable,
                                private val getWalletInfoUseCase: GetWalletInfoUseCase,
                                private val interactor: TransferInteractor,
                                private val navigator: TransferFragmentNavigator,
                                private val ioScheduler: Scheduler,
                                private val viewScheduler: Scheduler,
                                private val data: TransferFragmentData,
                                private val formatter: CurrencyFormatUtils) {

  fun onResume() {
    handleCurrencyChange()
  }

  fun present() {
    handleButtonClick()
    handleQrCodeButtonClick()
  }

  private fun handleCurrencyChange() {
    onResumeDisposables.add(view.getCurrencyChange()
        .subscribeOn(viewScheduler)
        .observeOn(ioScheduler)
        .switchMapSingle { currency ->
          getBalance(currency)
              .observeOn(viewScheduler)
              .doOnSuccess {
                val walletCurrency = mapToWalletCurrency(currency)
                view.showBalance(formatter.formatCurrency(it, walletCurrency), walletCurrency)
              }
        }
        .doOnError { it.printStackTrace() }
        .retry()
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun getBalance(currency: Currency): Single<BigDecimal> {
    return getWalletInfoUseCase(null, cached = false, updateFiat = false)
        .map { walletInfo ->
          val balance = walletInfo.walletBalance
          when (currency) {
            Currency.APPC_C -> balance.creditsBalance.token.amount
            Currency.APPC -> balance.appcBalance.token.amount
            Currency.ETH -> balance.ethBalance.token.amount
          }
        }
  }

  private fun mapToWalletCurrency(currency: Currency): WalletCurrency {
    return when (currency) {
      Currency.APPC -> WalletCurrency.APPCOINS
      Currency.APPC_C -> WalletCurrency.CREDITS
      Currency.ETH -> WalletCurrency.ETHEREUM
    }
  }

  private fun handleQrCodeResult(barcode: Barcode) {
    onResumeDisposables.add(Single.fromCallable { QRUri.parse(barcode.displayValue) }
        .observeOn(viewScheduler)
        .doOnSuccess { handleQRUri(it) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleQRUri(qrUri: QRUri) {
    if (qrUri.address != BarcodeCaptureActivity.ERROR_CODE) {
      view.showAddress(qrUri.address)
    } else {
      view.showCameraErrorToast()
    }
  }

  private fun handleQrCodeButtonClick() {
    disposables.add(view.getQrCodeButtonClick()
        .doOnNext { navigator.showQrCodeScreen() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun shouldBlockTransfer(currency: Currency): Single<Boolean> {
    return if (currency == Currency.APPC_C) {
      interactor.isWalletBlocked()
    } else {
      Single.just(false)
    }
  }

  private fun handleButtonClick() {
    disposables.add(view.getSendClick()
        .doOnNext {
          view.hideKeyboard()
          view.lockOrientation()
          navigator.showLoading()
        }
        .subscribeOn(viewScheduler)
        .observeOn(ioScheduler)
        .flatMapCompletable { data ->
          shouldBlockTransfer(data.currency)
              .flatMapCompletable {
                if (it) {
                  Completable.fromAction {
                    navigator.hideLoading()
                    view.unlockOrientation()
                    navigator.showWalletBlocked()
                  }
                      .subscribeOn(viewScheduler)
                } else {
                  makeTransaction(data)
                      .observeOn(viewScheduler)
                      .flatMapCompletable { status ->
                        handleTransferResult(data.currency, status, data.walletAddress,
                            data.amount)
                      }
                      .andThen {
                        navigator.hideLoading()
                        view.unlockOrientation()
                      }
                }
              }
        }
        .doOnError { handleError(it) }
        .retry()
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleError(throwable: Throwable) {
    navigator.hideLoading()
    view.unlockOrientation()
    if (throwable.isNoNetworkException()) {
      view.showNoNetworkError()
    } else {
      throwable.printStackTrace()
      view.showUnknownError()
    }
  }

  private fun makeTransaction(data: TransferData): Single<Status> {
    return when (data.currency) {
      Currency.APPC_C -> handleCreditsTransfer(data.walletAddress, data.amount)
      Currency.ETH -> interactor.validateEthTransferData(data.walletAddress, data.amount)
      Currency.APPC -> interactor.validateAppcTransferData(data.walletAddress, data.amount)
    }
  }

  private fun handleTransferResult(currency: Currency, status: Status,
                                   walletAddress: String, amount: BigDecimal): Completable {
    return Single.just(status)
        .subscribeOn(viewScheduler)
        .flatMapCompletable {
          when (status) {
            Status.API_ERROR, Status.UNKNOWN_ERROR, Status.NO_INTERNET -> Completable.fromCallable { view.showUnknownError() }
            Status.SUCCESS -> handleSuccess(currency, walletAddress, amount)
            Status.INVALID_AMOUNT -> Completable.fromCallable { view.showInvalidAmountError() }
            Status.INVALID_WALLET_ADDRESS -> Completable.fromCallable { view.showInvalidWalletAddress() }
            Status.NOT_ENOUGH_FUNDS -> Completable.fromCallable { view.showNotEnoughFunds() }
          }
        }
  }

  private fun handleSuccess(currency: Currency, walletAddress: String,
                            amount: BigDecimal): Completable {
    return when (currency) {
      Currency.APPC_C -> navigator.openAppcCreditsConfirmationView(walletAddress, amount, currency)
      Currency.APPC -> interactor.find()
          .flatMapCompletable {
            navigator.openAppcConfirmationView(it.address, walletAddress, amount)
          }
      Currency.ETH -> interactor.find()
          .flatMapCompletable {
            navigator.openEthConfirmationView(it.address, walletAddress, amount)
          }
    }
  }

  private fun handleCreditsTransfer(walletAddress: String,
                                    amount: BigDecimal): Single<Status> {
    return Single.zip(Single.timer(1, TimeUnit.SECONDS),
        interactor.transferCredits(walletAddress, amount, data.packageName),
        BiFunction { _: Long, status: Status -> status })
  }

  fun clearOnPause() = onResumeDisposables.clear()

  fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode == TransferFragmentNavigator.TRANSACTION_CONFIRMATION_REQUEST_CODE) {
      navigator.navigateBack()
    } else if (resultCode == CommonStatusCodes.SUCCESS && requestCode == TransferFragmentNavigator.BARCODE_READER_REQUEST_CODE) {
      data?.let {
        val barcode = it.getParcelableExtra<Barcode>(BarcodeCaptureActivity.BarcodeObject)
        println(barcode)
        barcode?.let { mBarcode -> handleQrCodeResult(mBarcode) }
      }
    }
  }

  fun stop() = disposables.clear()
}