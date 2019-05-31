package com.asfoundation.wallet.ui

import com.asfoundation.wallet.ui.iab.FiatValue
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.functions.Function3
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class BalanceFragmentPresenter(private val view: BalanceFragmentView,
                               private val networkScheduler: Scheduler,
                               private val viewScheduler: Scheduler) {


  fun present() {
    view.setupUI()
    requestTokenConversion()
  }

  private fun requestTokenConversion() {
    Single.zip(getCreditsBalance(), getAppcBalance(), getEthBalance(),
        Function3 { creditsBalance: Balance, appBalance: Balance, ethBalance: Balance ->
          getOverallBalance(creditsBalance, appBalance, ethBalance)
        }).subscribeOn(networkScheduler).observeOn(viewScheduler)
        .doOnSuccess { view.updateOverallBalance(it) }.subscribe()
  }

  private fun getCreditsBalance(): Single<Balance> {
    return Single.just(Balance(TokenValue(BigDecimal("1500").stripTrailingZeros()
        .setScale(2, BigDecimal.ROUND_DOWN), "AppCoins Credits", "APPC-C"),
        FiatValue(BigDecimal("500").stripTrailingZeros()
            .setScale(2, BigDecimal.ROUND_DOWN), "EUR", "€"))).delay(1, TimeUnit.SECONDS)
        .observeOn(viewScheduler)
        .doOnSuccess { view.updateTokenValue(it) }
  }

  private fun getAppcBalance(): Single<Balance> {
    return Single.just(Balance(TokenValue(BigDecimal("150").stripTrailingZeros()
        .setScale(2, BigDecimal.ROUND_DOWN), "AppCoins", "APPC"),
        FiatValue(BigDecimal("50").stripTrailingZeros()
            .setScale(2, BigDecimal.ROUND_DOWN), "EUR", "€"))).delay(3, TimeUnit.SECONDS)
        .observeOn(viewScheduler)
        .doOnSuccess { view.updateTokenValue(it) }
  }

  private fun getEthBalance(): Single<Balance> {
    return Single.just(Balance(TokenValue(BigDecimal("0.01").stripTrailingZeros()
        .setScale(2, BigDecimal.ROUND_DOWN), "Ethereum", "ETH"),
        FiatValue(BigDecimal("20").stripTrailingZeros()
            .setScale(2, BigDecimal.ROUND_DOWN), "EUR", "€"))).delay(5, TimeUnit.SECONDS)
        .observeOn(viewScheduler)
        .doOnSuccess { view.updateTokenValue(it) }
  }

  private fun getOverallBalance(creditsBalance: Balance, appcBalance: Balance,
                                ethBalance: Balance): FiatValue {
    val balance =
        creditsBalance.fiat.amount.add(appcBalance.fiat.amount).add(ethBalance.fiat.amount)
            .stripTrailingZeros()
            .setScale(2, BigDecimal.ROUND_DOWN)
    return FiatValue(balance, appcBalance.fiat.currency, appcBalance.fiat.symbol)
  }
}

data class Balance(val token: TokenValue, val fiat: FiatValue)
