package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.ui.TokenValue
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import java.math.BigDecimal

class BalanceFragmentPresenter(private val view: BalanceFragmentView,
                               private val balanceInteract: BalanceInteract,
                               private val networkScheduler: Scheduler,
                               private val viewScheduler: Scheduler,
                               private val disposables: CompositeDisposable) {


  companion object {
    const val APPC_CURRENCY = "APPC_CURRENCY"
    const val APPC_C_CURRENCY = "APPC_C_CURRENCY"
    const val ETH_CURRENCY = "ETH_CURRENCY"
  }

  fun present() {
    view.setupUI()
    requestTokenConversion()
    handleCreditsClick()
    handleAppcClick()
    handleEthClick()
    handleTopUpClick()
  }

  fun stop() {
    disposables.dispose()
  }

  private fun requestTokenConversion() {
    disposables.add(Observable.zip(getCreditsBalance(), getAppcBalance(), getEthBalance(),
        Function3 { creditsBalance: Balance, appBalance: Balance, ethBalance: Balance ->
          getOverallBalance(creditsBalance, appBalance, ethBalance)
        }).subscribeOn(networkScheduler).observeOn(viewScheduler)
        .subscribe({ view.updateOverallBalance(it) }, { it?.printStackTrace() }))
  }

  private fun getCreditsBalance(): Observable<Balance> {
    return balanceInteract.getCreditsBalance()
        .observeOn(viewScheduler).map { pair ->
          Balance(
              TokenValue(BigDecimal(pair.first.value),
                  APPC_C_CURRENCY,
                  pair.first.symbol),
              pair.second)
        }
        .doOnNext { view.updateTokenValue(it) }
  }

  private fun getAppcBalance(): Observable<Balance> {
    return balanceInteract.getAppcBalance()
        .observeOn(viewScheduler).map { pair ->
          Balance(
              TokenValue(BigDecimal(pair.first.value),
                  APPC_CURRENCY,
                  pair.first.symbol),
              pair.second)
        }.doOnNext { view.updateTokenValue(it) }
  }

  private fun getEthBalance(): Observable<Balance> {
    return balanceInteract.getEthBalance()
        .observeOn(viewScheduler).map { pair ->
          Balance(
              TokenValue(BigDecimal(pair.first.value),
                  ETH_CURRENCY,
                  pair.first.symbol),
              pair.second)
        }
        .doOnNext { view.updateTokenValue(it) }
  }

  private fun getOverallBalance(creditsBalance: Balance, appcBalance: Balance,
                                ethBalance: Balance): FiatValue {
    var balance = BigDecimal("-1")
    if (creditsBalance.fiat.amount.compareTo(BigDecimal("-1")) == 1) {
         balance = creditsBalance.fiat.amount
    }

    if (appcBalance.fiat.amount.compareTo(BigDecimal("-1")) == 1) {
      balance = if (balance.compareTo(BigDecimal("-1")) == 1) {
        balance.add(appcBalance.fiat.amount)
      } else {
        appcBalance.fiat.amount
      }
    }

    if (ethBalance.fiat.amount.compareTo(BigDecimal("-1")) == 1) {
      balance = if (balance.compareTo(BigDecimal("-1")) == 1) {
        balance.add(ethBalance.fiat.amount)
      } else {
        ethBalance.fiat.amount
      }
    }

    if (balance.compareTo(BigDecimal("-1")) == 1) {
     balance.stripTrailingZeros()
        .setScale(2, BigDecimal.ROUND_DOWN)
    }

    return FiatValue(balance, appcBalance.fiat.currency, appcBalance.fiat.symbol)
  }

  private fun handleCreditsClick() {
    disposables.add(view.getCreditClick()
        .doOnNext { view.showCreditsDetails() }
        .subscribe())
  }

  private fun handleAppcClick() {
    disposables.add(view.getAppcClick()
        .doOnNext { view.showAppcDetails() }
        .subscribe())
  }

  private fun handleEthClick() {
    disposables.add(view.getEthClick()
        .doOnNext { view.showEthDetails() }
        .subscribe())
  }

  private fun handleTopUpClick() {
    disposables.add(view.getTopUpClick()
        .doOnNext { view.showTopUpScreen() }
        .subscribe())
  }
}

data class Balance(val token: TokenValue, val fiat: FiatValue)