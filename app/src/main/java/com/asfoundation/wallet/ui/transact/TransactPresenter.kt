package com.asfoundation.wallet.ui.transact

import com.appcoins.wallet.appcoins.rewards.AppcoinsRewardsRepository
import com.asfoundation.wallet.interact.FindDefaultWalletInteract
import com.asfoundation.wallet.util.BalanceUtils
import com.asfoundation.wallet.util.QRUri
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class TransactPresenter(private val view: TransactFragmentView,
                        private val disposables: CompositeDisposable,
                        private val interactor: TransferInteractor,
                        private val ioScheduler: Scheduler,
                        private val viewScheduler: Scheduler,
                        private val walletInteract: FindDefaultWalletInteract,
                        private val packageName: String) {

  fun present() {
    handleButtonClick()
    handleQrCodeButtonClick()
    handleQrCodeResult()
    handleCurrencyChange()
  }

  private fun handleCurrencyChange() {
    disposables.add(view.getCurrencyChange()
        .subscribeOn(viewScheduler)
        .observeOn(ioScheduler)
        .flatMapSingle {
          getBalance(it).map { balance -> BalanceUtils.weiToEth(balance) }
              .observeOn(viewScheduler)
              .doOnSuccess { balance -> view.showBalance(balance, it) }
        }
        .doOnError { it.printStackTrace() }
        .retry()
        .subscribe())
  }

  private fun getBalance(currency: TransactFragmentView.Currency): Single<BigDecimal> {
    return when (currency) {
      TransactFragmentView.Currency.APPC_C -> interactor.getCreditsBalance()
      TransactFragmentView.Currency.APPC -> interactor.getAppcoinsBalance()
      TransactFragmentView.Currency.ETH -> interactor.getEthBalance()
    }
  }

  private fun handleQrCodeResult() {
    disposables.add(
        view.getQrCodeResult()
            .observeOn(ioScheduler)
            .map {
              QRUri.parse(it.displayValue)
            }
            .observeOn(viewScheduler)
            .doOnNext { qrCode ->
              qrCode?.let { view.showAddress(it.address) }
            }
            .subscribe())
  }

  private fun handleQrCodeButtonClick() {
    disposables.add(view.getQrCodeButtonClick()
        .doOnNext { view.showQrCodeScreen() }
        .subscribe())
  }

  private fun handleButtonClick() {
    disposables.add(view.getSendClick()
        .doOnNext { view.showLoading() }
        .subscribeOn(viewScheduler)
        .observeOn(ioScheduler)
        .flatMapCompletable {
          makeTransaction(it).observeOn(viewScheduler).flatMapCompletable { status ->
            handleTransferResult(it.currency, status, it.walletAddress, it.amount)
          }
              .andThen { view.hideLoading() }
        }
        .doOnError { error ->
          error.printStackTrace()
          view.hideLoading()
        }
        .retry()
        .subscribe { })
  }

  private fun makeTransaction(
      data: TransactFragmentView.TransactData): Single<AppcoinsRewardsRepository.Status> {
    return when (data.currency) {
      TransactFragmentView.Currency.APPC_C -> handleCreditsTransfer(data.walletAddress,
          data.amount)
      TransactFragmentView.Currency.ETH -> Single.just(AppcoinsRewardsRepository.Status.SUCCESS)
      TransactFragmentView.Currency.APPC -> Single.just(AppcoinsRewardsRepository.Status.SUCCESS)
    }
  }

  private fun handleTransferResult(
      currency: TransactFragmentView.Currency,
      status: AppcoinsRewardsRepository.Status,
      walletAddress: String,
      amount: BigDecimal): Completable {
    return Single.just(status).subscribeOn(viewScheduler).flatMapCompletable {
      when (status) {
        AppcoinsRewardsRepository.Status.API_ERROR,
        AppcoinsRewardsRepository.Status.UNKNOWN_ERROR,
        AppcoinsRewardsRepository.Status.NO_INTERNET ->
          Completable.fromCallable { view.showUnknownError() }
        AppcoinsRewardsRepository.Status.SUCCESS -> {
          handleSuccess(currency, walletAddress, amount)
        }
        AppcoinsRewardsRepository.Status.INVALID_AMOUNT -> Completable.fromCallable { view.showInvalidAmountError() }
        AppcoinsRewardsRepository.Status.INVALID_WALLET_ADDRESS -> Completable.fromCallable { view.showInvalidWalletAddress() }
        AppcoinsRewardsRepository.Status.NOT_ENOUGH_FUNDS -> Completable.fromCallable { view.showNotEnoughFunds() }
      }
    }


  }

  private fun handleSuccess(
      currency: TransactFragmentView.Currency,
      walletAddress: String, amount: BigDecimal): Completable {
    return when (currency) {
      TransactFragmentView.Currency.APPC_C ->
        view.openAppcCreditsConfirmationView(walletAddress, amount, currency)
      TransactFragmentView.Currency.APPC -> walletInteract.find()
          .flatMapCompletable { wallet ->
            view.openAppcConfirmationView(wallet.address, walletAddress, amount)
          }
      TransactFragmentView.Currency.ETH -> walletInteract.find()
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

  fun clear() {
    disposables.clear()
  }
}