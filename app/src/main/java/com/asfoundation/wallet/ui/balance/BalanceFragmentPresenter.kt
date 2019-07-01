package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.ui.TokenValue
import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
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
    requestTokenConversion()
    handleTokenDetailsClick()
    handleTopUpClick()
  }

  fun stop() {
    disposables.dispose()
  }

  private fun requestTokenConversion() {
    disposables.add(Observable.zip(getCreditsBalance(), getAppcBalance(), getEthBalance(),
        Function3 { creditsBalance: Balance, appBalance: Balance, ethBalance: Balance ->
          getOverallBalance(creditsBalance, appBalance, ethBalance)
        }).subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .subscribe({ view.updateOverallBalance(it) }, { it?.printStackTrace() }))
  }

  private fun getCreditsBalance(): Observable<Balance> {
    return balanceInteract.getCreditsBalance()
        .observeOn(viewScheduler)
        .map { pair ->
          Balance(
              TokenValue(pair.first.value,
                  APPC_C_CURRENCY,
                  pair.first.symbol),
              pair.second)
        }
        .doOnNext { view.updateTokenValue(it) }
  }

  private fun getAppcBalance(): Observable<Balance> {
    return balanceInteract.getAppcBalance()
        .observeOn(viewScheduler)
        .map { pair ->
          Balance(
              TokenValue(pair.first.value,
                  APPC_CURRENCY,
                  pair.first.symbol),
              pair.second)
        }
        .doOnNext { view.updateTokenValue(it) }
  }

  private fun getEthBalance(): Observable<Balance> {
    return balanceInteract.getEthBalance()
        .observeOn(viewScheduler)
        .map { pair ->
          Balance(
              TokenValue(pair.first.value,
                  ETH_CURRENCY,
                  pair.first.symbol),
              pair.second)
        }
        .doOnNext { view.updateTokenValue(it) }
  }

  private fun getOverallBalance(creditsBalance: Balance, appcBalance: Balance,
                                ethBalance: Balance): FiatValue {
    var balance = getAddBalanceValue(BIG_DECIMAL_MINUS_ONE, creditsBalance.fiat.amount)
    balance = getAddBalanceValue(balance, appcBalance.fiat.amount)
    balance = getAddBalanceValue(balance, ethBalance.fiat.amount)

    if (balance.compareTo(BIG_DECIMAL_MINUS_ONE) == 1) {
      balance.stripTrailingZeros()
          .setScale(2, BigDecimal.ROUND_DOWN)
    }

    return FiatValue(balance, appcBalance.fiat.currency, appcBalance.fiat.symbol)
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

  private fun getAddBalanceValue(currentValue: BigDecimal, value: BigDecimal): BigDecimal {
    return if (value.compareTo(BIG_DECIMAL_MINUS_ONE) == 1) {
      if (currentValue.compareTo(BIG_DECIMAL_MINUS_ONE) == 1) {
        currentValue.add(value)
      } else {
        value
      }
    } else {
      currentValue
    }
  }
}

data class Balance(val token: TokenValue, val fiat: FiatValue)