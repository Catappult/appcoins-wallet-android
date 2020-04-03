package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class BalanceFragmentPresenter(private val view: BalanceFragmentView,
                               private val balanceInteract: BalanceInteract,
                               private val networkScheduler: Scheduler,
                               private val viewScheduler: Scheduler,
                               private val disposables: CompositeDisposable,
                               private val formatter: CurrencyFormatUtils) {


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
    updateAppcBalance(balanceScreenModel.appcBalance)
    updateCreditsBalance(balanceScreenModel.creditsBalance)
    updateEthereumBalance(balanceScreenModel.ethBalance)
    updateOverallBalance(balanceScreenModel.overallFiat)
  }

  private fun handleTokenDetailsClick() {
    disposables.add(
        Observable.merge(view.getCreditClick(), view.getAppcClick(), view.getEthClick())
            .throttleFirst(500, TimeUnit.MILLISECONDS)
            .map { view.showTokenDetails(it) }
            .subscribe())
  }

  private fun handleTopUpClick() {
    disposables.add(view.getTopUpClick()
        .throttleFirst(1, TimeUnit.SECONDS)
        .doOnNext { view.showTopUpScreen() }
        .subscribe())
  }

  private fun updateAppcBalance(balance: TokenBalance) {
    var tokenBalance = "-1"
    var fiatBalance = "-1"
    if (balance.token.amount.compareTo(BigDecimal("-1")) == 1) {
      tokenBalance =
          formatter.formatCurrency(balance.token.amount.toDouble(), WalletCurrency.APPCOINS)
      fiatBalance = formatter.formatCurrency(balance.fiat)
    }
    view.updateTokenValue(tokenBalance, fiatBalance, WalletCurrency.APPCOINS, balance.fiat.symbol)
  }

  private fun updateCreditsBalance(balance: TokenBalance) {
    var tokenBalance = "-1"
    var fiatBalance = "-1"
    if (balance.token.amount.compareTo(BigDecimal("-1")) == 1) {
      tokenBalance =
          formatter.formatCurrency(balance.token.amount.toDouble(), WalletCurrency.CREDITS)
      fiatBalance = formatter.formatCurrency(balance.fiat)
    }
    view.updateTokenValue(tokenBalance, fiatBalance, WalletCurrency.CREDITS, balance.fiat.symbol)
  }

  private fun updateEthereumBalance(balance: TokenBalance) {
    var tokenBalance = "-1"
    var fiatBalance = "-1"
    if (balance.token.amount.compareTo(BigDecimal("-1")) == 1) {
      tokenBalance =
          formatter.formatCurrency(balance.token.amount.toDouble(), WalletCurrency.ETHEREUM)
      fiatBalance = formatter.formatCurrency(balance.fiat)
    }
    view.updateTokenValue(tokenBalance, fiatBalance, WalletCurrency.ETHEREUM, balance.fiat.symbol)
  }

  private fun updateOverallBalance(balance: FiatValue) {
    var overallBalance = "-1"
    if (balance.amount.compareTo(BigDecimal("-1")) == 1) {
      overallBalance = formatter.formatCurrency(balance)
    }
    view.updateOverallBalance(overallBalance, balance.currency, balance.symbol)
  }
}