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
    requestBalances()
    handleTokenDetailsClick()
    handleTopUpClick()
  }

  fun stop() {
    disposables.dispose()
  }

  private fun requestBalances() {
    disposables.add(
        balanceInteract.requestTokenConversion()
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnNext { updateUI(it) }
            .doOnError { it?.printStackTrace() }
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

  private fun handleTopUpClick() {
    disposables.add(view.getTopUpClick()
        .doOnNext { view.showTopUpScreen() }
        .subscribe())
  }

}