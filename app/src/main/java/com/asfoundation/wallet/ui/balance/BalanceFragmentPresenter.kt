package com.asfoundation.wallet.ui.balance

import com.asfoundation.wallet.billing.analytics.WalletsAnalytics
import com.asfoundation.wallet.billing.analytics.WalletsEventSender
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.verification.network.VerificationStatus
import com.google.android.material.bottomsheet.BottomSheetBehavior
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.math.BigDecimal
import java.util.concurrent.TimeUnit

class BalanceFragmentPresenter(private val view: BalanceFragmentView,
                               private val activityView: BalanceActivityView?,
                               private val balanceInteractor: BalanceInteractor,
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
    requestBalances()
    handleTokenDetailsClick()
    handleCopyClick()
    handleQrCodeClick()
    handleBackupClick()
    handleBackPress()
    handleSetupTooltip()
    handleTooltipBackupClick()
    handleTooltipLaterClick()
    handleVerifyWalletClick()
    handleInsertCodeClick()
    handleCachedWalletInfoDisplay()
    handleBottomSheetStateChanged()
  }

  private fun handleBottomSheetStateChanged() {
    disposables.add(view.getBottomSheetStateChanged()
        .filter { it == BottomSheetBehavior.STATE_EXPANDED || it == BottomSheetBehavior.STATE_COLLAPSED }
        .distinctUntilChanged()
        .doOnNext { state ->
          if (state == BottomSheetBehavior.STATE_EXPANDED) {
            walletsEventSender.sendAction("wallet_list_expand")
          }
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun onResume() {
    handleWalletInfoDisplay()
  }

  private fun handleTooltipLaterClick() {
    disposables.add(view.getTooltipDismissClick()
        .doOnNext {
          view.dismissTooltip()
          balanceInteractor.saveSeenBackupTooltip()
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleTooltipBackupClick() {
    disposables.add(view.getTooltipBackupButton()
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .flatMapSingle { balanceInteractor.requestActiveWalletAddress() }
        .observeOn(viewScheduler)
        .doOnNext {
          balanceInteractor.saveSeenBackupTooltip()
          activityView?.navigateToBackupView(it)
          view.dismissTooltip()
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

  private fun handleSetupTooltip() {
    disposables.add(Single.just(balanceInteractor.hasSeenBackupTooltip())
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

  private fun handleVerifyWalletClick() {
    disposables.add(view.getVerifyWalletClick()
        .doOnNext { walletsEventSender.sendVerifyAction("verify_wallet", "start_verification") }
        .observeOn(viewScheduler)
        .doOnNext { view.openWalletVerificationScreen() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleInsertCodeClick() {
    disposables.add(view.getInsertCodeClick()
        .doOnNext { walletsEventSender.sendVerifyAction("verify_wallet", "insert_code") }
        .observeOn(viewScheduler)
        .doOnNext { view.openWalletVerificationScreen() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleCachedWalletInfoDisplay() {
    disposables.add(balanceInteractor.requestActiveWalletAddress()
        .observeOn(viewScheduler)
        .doOnSuccess { handleValidationCache(it) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleWalletInfoDisplay() {
    disposables.add(balanceInteractor.getSignedCurrentWalletAddress()
        .observeOn(viewScheduler)
        .doOnSuccess { view.setWalletAddress(it.address) }
        .observeOn(networkScheduler)
        .flatMap { balanceInteractor.isWalletValid(it.address, it.signedAddress) }
        .observeOn(viewScheduler)
        .doOnSuccess {
          when (it.status) {
            BalanceVerificationStatus.VERIFIED -> displayWalletVerifiedStatus()
            BalanceVerificationStatus.UNVERIFIED -> displayWalletUnverifiedStatus()
            BalanceVerificationStatus.CODE_REQUESTED -> displayWalletCodeRequestedStatus()
            BalanceVerificationStatus.NO_NETWORK, BalanceVerificationStatus.ERROR -> {
              handleNoNetwork(it.address)
            }
          }
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun requestBalances() {
    disposables.add(balanceInteractor.requestTokenConversion()
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
        Observable.merge(view.getCreditClick()
            .doOnNext { walletsEventSender.sendAction("currency_details_credits") },
            view.getAppcClick()
                .doOnNext { walletsEventSender.sendAction("currency_details_appc") },
            view.getEthClick()
                .doOnNext { walletsEventSender.sendAction("currency_details_eth") })
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
        .doOnNext { walletsEventSender.sendAction("wallet_copy") }
        .flatMapSingle { balanceInteractor.requestActiveWalletAddress() }
        .observeOn(viewScheduler)
        .doOnNext { view.setAddressToClipBoard(it) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleQrCodeClick() {
    disposables.add(view.getQrCodeClick()
        .doOnNext { walletsEventSender.sendAction("wallet_qr_code") }
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .observeOn(viewScheduler)
        .doOnNext { view.showQrCodeView() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleBackupClick() {
    disposables.add(view.getBackupClick()
        .doOnNext { walletsEventSender.sendAction("wallet_backup") }
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .flatMapSingle { balanceInteractor.requestActiveWalletAddress() }
        .observeOn(viewScheduler)
        .doOnNext { activityView?.navigateToBackupView(it) }
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

  private fun handleValidationCache(address: String) {
    when (balanceInteractor.getCachedVerificationStatus(address)) {
      VerificationStatus.VERIFIED -> displayWalletVerifiedStatus()
      VerificationStatus.UNVERIFIED -> displayWalletUnverifiedStatus()
      VerificationStatus.CODE_REQUESTED -> displayWalletCodeRequestedStatus(true)
      else -> displayWalletUnverifiedStatus(true)
    }
  }

  private fun handleNoNetwork(address: String) {
    when (balanceInteractor.getCachedVerificationStatus(address)) {
      VerificationStatus.VERIFIED -> displayWalletVerifiedStatus()
      VerificationStatus.UNVERIFIED -> displayWalletUnverifiedStatus(true)
      VerificationStatus.CODE_REQUESTED -> displayWalletCodeRequestedStatus(true)
      else -> displayWalletUnverifiedStatus(true)
    }
  }

  private fun displayWalletVerifiedStatus() {
    view.showVerifiedWalletChip()
    view.hideUnverifiedWalletChip()
    view.hideRequestedCodeWalletChip()
  }

  private fun displayWalletUnverifiedStatus(disabled: Boolean = false) {
    view.showUnverifiedWalletChip()
    view.hideVerifiedWalletChip()
    view.hideRequestedCodeWalletChip()
    if (disabled) view.disableVerifyWalletButton() else view.enableVerifyWalletButton()
  }

  private fun displayWalletCodeRequestedStatus(disabled: Boolean = false) {
    view.hideUnverifiedWalletChip()
    view.hideVerifiedWalletChip()
    view.showRequestedCodeWalletChip()
    if (disabled) view.disableInserCodeButton() else view.enableInsertCodeButton()
  }

  fun saveSeenToolTip() {
    balanceInteractor.saveSeenBackupTooltip()
  }

  fun stop() = disposables.clear()
}