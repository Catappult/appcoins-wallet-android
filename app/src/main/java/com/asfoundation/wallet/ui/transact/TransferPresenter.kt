package com.asfoundation.wallet.ui.transact

import android.os.Bundle
import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.ui.barcode.BarcodeCaptureActivity
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.QRUri
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.util.isNoNetworkException
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class TransferPresenter(private val view: TransferFragmentView,
                        private val disposables: CompositeDisposable,
                        private val onResumeDisposables: CompositeDisposable,
                        private val interactor: TransferInteractor,
                        private val ioScheduler: Scheduler,
                        private val viewScheduler: Scheduler,
                        private val walletInteract: FindDefaultWalletInteract,
                        private val walletBlockedInteract: WalletBlockedInteract,
                        private val packageName: String,
                        private val formatter: CurrencyFormatUtils,
                        private val transferActivity: TransferActivityView,
                        private val preferencesRepositoryType: PreferencesRepositoryType) {

  private var cachedData: TransferFragmentView.TransferData? = null

  fun onResume() {
    handleQrCodeResult()
    handleCurrencyChange()
  }

  fun present(savedInstanceState: Bundle?) {
    savedInstanceState?.let {
      cachedData = savedInstanceState.getSerializable(DATA) as TransferFragmentView.TransferData
    }
    handleButtonClick()
    handleQrCodeButtonClick()
    handleAuthenticationResult()
  }

  private fun handleCurrencyChange() {
    onResumeDisposables.add(view.getCurrencyChange()
        .subscribeOn(viewScheduler)
        .observeOn(ioScheduler)
        .switchMapSingle { currency ->
          getBalance(currency)
              .observeOn(viewScheduler)
              .doOnSuccess {
                val walletCurrency = WalletCurrency.mapToWalletCurrency(currency)
                view.showBalance(formatter.formatCurrency(it, walletCurrency), walletCurrency)
              }
        }
        .doOnError { it.printStackTrace() }
        .retry()
        .subscribe())
  }

  private fun getBalance(currency: TransferFragmentView.Currency): Single<BigDecimal> {
    return when (currency) {
      TransferFragmentView.Currency.APPC_C -> interactor.getCreditsBalance()
      TransferFragmentView.Currency.APPC -> interactor.getAppcoinsBalance()
      TransferFragmentView.Currency.ETH -> interactor.getEthBalance()
    }
  }

  private fun handleQrCodeResult() {
    onResumeDisposables.add(
        view.getQrCodeResult()
            .observeOn(ioScheduler)
            .map { QRUri.parse(it.displayValue) }
            .observeOn(viewScheduler)
            .doOnNext { handleQRUri(it) }
            .subscribe())
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
        .doOnNext { view.showQrCodeScreen() }
        .subscribe())
  }

  private fun shouldBlockTransfer(
      currency: TransferFragmentView.Currency): Single<Boolean> {
    return if (currency == TransferFragmentView.Currency.APPC_C) {
      walletBlockedInteract.isWalletBlocked()
    } else {
      Single.just(false)
    }
  }

  private fun handleButtonClick() {
    disposables.add(view.getSendClick()
        .doOnNext { view.showLoading() }
        .subscribeOn(viewScheduler)
        .observeOn(ioScheduler)
        .flatMapCompletable { data ->
          shouldBlockTransfer(data.currency)
              .flatMapCompletable {
                if (it) {
                  Completable.fromAction { view.showWalletBlocked() }
                      .subscribeOn(viewScheduler)
                } else {
                  if (preferencesRepositoryType.hasAuthenticationPermission()) {
                    Completable.fromAction {
                      this.cachedData = data
                      transferActivity.showAuthenticationActivity()
                    }
                  } else {
                    makeTransaction(data)
                        .observeOn(viewScheduler)
                        .flatMapCompletable { status ->
                          handleTransferResult(data.currency, status, data.walletAddress,
                              data.amount)
                        }
                        .andThen { view.hideLoading() }
                  }
                }
              }
        }
        .doOnError { handleError(it) }
        .retry()
        .subscribe { })
  }

  private fun handleError(throwable: Throwable) {
    view.hideLoading()
    if (throwable.isNoNetworkException()) {
      view.showNoNetworkError()
    } else {
      throwable.printStackTrace()
      view.showUnknownError()
    }
  }

  private fun makeTransaction(
      data: TransferFragmentView.TransferData): Single<AppcoinsRewardsRepository.Status> {
    return when (data.currency) {
      TransferFragmentView.Currency.APPC_C -> handleCreditsTransfer(data.walletAddress,
          data.amount)
      TransferFragmentView.Currency.ETH -> interactor.validateEthTransferData(data.walletAddress,
          data.amount)
      TransferFragmentView.Currency.APPC -> interactor.validateAppcTransferData(data.walletAddress,
          data.amount)
    }
  }

  private fun handleTransferResult(currency: TransferFragmentView.Currency,
                                   status: AppcoinsRewardsRepository.Status, walletAddress: String,
                                   amount: BigDecimal): Completable {
    return Single.just(status)
        .subscribeOn(viewScheduler)
        .flatMapCompletable {
          when (status) {
            AppcoinsRewardsRepository.Status.API_ERROR,
            AppcoinsRewardsRepository.Status.UNKNOWN_ERROR,
            AppcoinsRewardsRepository.Status.NO_INTERNET ->
              Completable.fromCallable { view.showUnknownError() }
            AppcoinsRewardsRepository.Status.SUCCESS -> handleSuccess(currency, walletAddress,
                amount)
            AppcoinsRewardsRepository.Status.INVALID_AMOUNT -> Completable.fromCallable { view.showInvalidAmountError() }
            AppcoinsRewardsRepository.Status.INVALID_WALLET_ADDRESS -> Completable.fromCallable { view.showInvalidWalletAddress() }
            AppcoinsRewardsRepository.Status.NOT_ENOUGH_FUNDS -> Completable.fromCallable { view.showNotEnoughFunds() }
          }
        }
  }

  private fun handleSuccess(
      currency: TransferFragmentView.Currency,
      walletAddress: String, amount: BigDecimal): Completable {
    return when (currency) {
      TransferFragmentView.Currency.APPC_C ->
        view.openAppcCreditsConfirmationView(walletAddress, amount, currency)
      TransferFragmentView.Currency.APPC -> walletInteract.find()
          .flatMapCompletable { wallet ->
            view.openAppcConfirmationView(wallet.address, walletAddress, amount)
          }
      TransferFragmentView.Currency.ETH -> walletInteract.find()
          .flatMapCompletable { wallet ->
            view.openEthConfirmationView(wallet.address, walletAddress, amount)
          }
    }
  }

  private fun handleCreditsTransfer(walletAddress: String,
                                    amount: BigDecimal): Single<AppcoinsRewardsRepository.Status> {
    return Single.zip(Single.timer(1, TimeUnit.SECONDS),
        interactor.transferCredits(walletAddress, amount, packageName),
        BiFunction { _: Long, status: AppcoinsRewardsRepository.Status -> status })
  }

  private fun handleAuthenticationResult() {
    disposables.add(view.onAuthenticationResult()
        .observeOn(viewScheduler)
        .flatMapCompletable {
          if (it) {
            if (cachedData != null) {
              makeTransaction(cachedData!!)
                  .subscribeOn(ioScheduler)
                  .observeOn(viewScheduler)
                  .doOnSuccess { view.hideLoading() }
                  .flatMapCompletable { status ->
                    handleTransferResult(cachedData!!.currency, status, cachedData!!.walletAddress,
                        cachedData!!.amount)
                  }
            } else {
              Completable.fromAction {
                view.hideLoading()
                view.showUnknownError()
              }
            }
          } else {
            Completable.fromAction {
              view.hideLoading()
              cachedData = null
            }
          }
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun clearOnPause() {
    onResumeDisposables.clear()
  }

  fun stop() {
    disposables.clear()
  }

  fun onSaveInstance(outState: Bundle) {
    outState.putSerializable(DATA, cachedData)
  }

  companion object {
    const val DATA = "data_key"
  }
}