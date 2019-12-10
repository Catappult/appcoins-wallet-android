package com.asfoundation.wallet.ui.balance

import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class BalanceFragmentPresenter(private val view: BalanceFragmentView,
                               private val balanceInteract: BalanceInteract,
                               private val networkScheduler: Scheduler,
                               private val viewScheduler: Scheduler,
                               private val disposables: CompositeDisposable) {


  companion object {
    const val APPC_CURRENCY = "APPC_CURRENCY"
    const val APPC_C_CURRENCY = "APPC_C_CURRENCY"
    const val ETH_CURRENCY = "ETH_CURRENCY"
    val BIG_DECIMAL_MINUS_ONE = BigDecimal("-1")
  }

  fun present() {
    view.setupUI()
    requestActiveWalletAddress()
    requestBalances()
    handleTokenDetailsClick()
    handleCopyClick()
    handleQrCodeClick()
    handleBackPress()

  }

  private fun handleBackPress() {
    disposables.add(view.backPressed()
        .observeOn(viewScheduler)
        .doOnNext { view.handleBackPress() }
        .subscribe())
  }

  private fun requestActiveWalletAddress() {
    disposables.add(
        balanceInteract.requestActiveWalletAddress()
            .doOnSuccess { view.setWalletAddress(it) }
            .subscribe({}, { it.printStackTrace() }))
  }

  private fun requestBalances() {
    disposables.add(
        balanceInteract.requestTokenConversion()
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnNext { updateUI(it) }
            .doOnError { it.printStackTrace() }
            .subscribe()
    )
  }

  private fun updateUI(balanceScreenModel: BalanceScreenModel) {
    view.updateTokenValue(balanceScreenModel.creditsBalance)
    view.updateTokenValue(balanceScreenModel.appcBalance)
    view.updateTokenValue(balanceScreenModel.ethBalance)
    view.updateOverallBalance(balanceScreenModel.overallFiat)
  }

  private fun handleTokenDetailsClick() {
    disposables.add(
        Observable.merge(view.getCreditClick(), view.getAppcClick(),
            view.getEthClick()).throttleFirst(500,
            TimeUnit.MILLISECONDS).map { view.showTokenDetails(it) }.subscribe())
  }

  private fun handleCopyClick() {
    disposables.add(
        view.getCopyClick()
            .flatMapSingle { balanceInteract.requestActiveWalletAddress() }
            .observeOn(viewScheduler)
            .doOnNext { view.setAddressToClipBoard(it) }
            .subscribe())
  }

  private fun handleQrCodeClick() {
    disposables.add(
        view.getQrCodeClick()
            .observeOn(viewScheduler)
            .doOnNext { view.showQrCodeView() }
            .subscribe())
  }

  fun stop() {
    disposables.dispose()
  }
}