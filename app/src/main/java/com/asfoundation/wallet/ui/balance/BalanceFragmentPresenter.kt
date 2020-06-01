package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class BalanceFragmentPresenter(private val view: BalanceFragmentView,
                               private val activityView: BalanceActivityView?,
                               private val balanceInteract: BalanceInteract,
                               private val walletsEventSender: WalletsEventSender,
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
    requestActiveWalletAddress()
    requestBalances()
    handleTokenDetailsClick()
    handleCopyClick()
    handleQrCodeClick()
    handleBackupClick()
    handleBackPress()
    handleSetupTooltip()
    handleTooltipBackupClick()
    handleTooltipLaterClick()
  }

  private fun handleTooltipLaterClick() {
    disposables.add(view.getTooltipDismissClick()
        .doOnNext {
          view.dismissTooltip()
          balanceInteract.saveSeenBackupTooltip()
        }
        .doOnNext {
          walletsEventSender.sendCreateBackupEvent(WalletsAnalytics.ACTION_CREATE,
              WalletsAnalytics.CONTEXT_WALLET_TOOLTIP, WalletsAnalytics.STATUS_SUCCESS)
        }
        .doOnError {
          walletsEventSender.sendCreateBackupEvent(WalletsAnalytics.ACTION_CREATE,
              WalletsAnalytics.CONTEXT_WALLET_TOOLTIP, WalletsAnalytics.STATUS_FAIL, it.message)
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleTooltipBackupClick() {
    disposables.add(view.getTooltipBackupButton()
        .flatMapSingle { balanceInteract.requestActiveWalletAddress() }
        .observeOn(viewScheduler)
        .doOnNext {
          balanceInteract.saveSeenBackupTooltip()
          activityView?.navigateToBackupView(it)
          view.dismissTooltip()
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleSetupTooltip() {
    disposables.add(Single.just(balanceInteract.hasSeenBackupTooltip())
        .subscribeOn(networkScheduler)
        .filter { !it }
        .observeOn(viewScheduler)
        .doOnSuccess { view.setTooltip() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleBackPress() {
    disposables.add(Observable.merge(view.backPressed(), view.homeBackPressed())
        .observeOn(viewScheduler)
        .doOnNext { view.handleBackPress() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun requestActiveWalletAddress() {
    disposables.add(balanceInteract.requestActiveWalletAddress()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { view.setWalletAddress(it) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun requestBalances() {
    disposables.add(balanceInteract.requestTokenConversion()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnNext { updateUI(it) }
        .subscribe({}, { it.printStackTrace() })
    )
  }

  private fun updateUI(balanceScreenModel: BalanceScreenModel) {
    updateTokenBalance(balanceScreenModel.appcBalance, WalletCurrency.APPCOINS)
    updateTokenBalance(balanceScreenModel.creditsBalance, WalletCurrency.CREDITS)
    updateTokenBalance(balanceScreenModel.ethBalance, WalletCurrency.ETHEREUM)
    updateOverallBalance(balanceScreenModel.overallFiat)
  }

  private fun handleTokenDetailsClick() {
    disposables.add(
        Observable.merge(view.getCreditClick(), view.getAppcClick(), view.getEthClick())
            .throttleFirst(500, TimeUnit.MILLISECONDS)
            .map { view.showTokenDetails(it) }
            .subscribe({}, { it.printStackTrace() }))
  }

  private fun updateTokenBalance(balance: TokenBalance, currency: WalletCurrency) {
    var tokenBalance = "-1"
    var fiatBalance = "-1"
    if (balance.token.amount.compareTo(BigDecimal("-1")) == 1) {
      tokenBalance = formatter.formatCurrency(balance.token.amount, currency)
      fiatBalance = formatter.formatCurrency(balance.fiat.amount)
    }
    view.updateTokenValue(tokenBalance, fiatBalance, currency, balance.fiat.symbol)
  }

  private fun updateOverallBalance(balance: FiatValue) {
    var overallBalance = "-1"
    if (balance.amount.compareTo(BigDecimal("-1")) == 1) {
      overallBalance = formatter.formatCurrency(balance.amount)
    }
    view.updateOverallBalance(overallBalance, balance.currency, balance.symbol)
  }

  private fun handleCopyClick() {
    disposables.add(view.getCopyClick()
        .flatMapSingle { balanceInteract.requestActiveWalletAddress() }
        .observeOn(viewScheduler)
        .doOnNext { view.setAddressToClipBoard(it) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleQrCodeClick() {
    disposables.add(view.getQrCodeClick()
        .observeOn(viewScheduler)
        .doOnNext { view.showQrCodeView() }
        .doOnNext {
          walletsEventSender.sendCreateBackupEvent(WalletsAnalytics.ACTION_CREATE,
              WalletsAnalytics.CONTEXT_WALLET_BALANCE, WalletsAnalytics.STATUS_SUCCESS)
        }
        .doOnError {
          walletsEventSender.sendCreateBackupEvent(WalletsAnalytics.ACTION_CREATE,
              WalletsAnalytics.CONTEXT_WALLET_BALANCE, WalletsAnalytics.STATUS_FAIL)
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleBackupClick() {
    disposables.add(view.getBackupClick()
        .flatMapSingle { balanceInteract.requestActiveWalletAddress() }
        .observeOn(viewScheduler)
        .doOnNext { activityView?.navigateToBackupView(it) }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun saveSeenToolTip() {
    balanceInteract.saveSeenBackupTooltip()
  }

  fun stop() = disposables.clear()
}