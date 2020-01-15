package com.asfoundation.wallet.ui.iab

import android.util.Log
import com.asf.wallet.R
import com.asfoundation.wallet.ui.balance.BalanceInteract
import com.asfoundation.wallet.ui.iab.MergedAppcoinsFragment.Companion.APPC
import com.asfoundation.wallet.ui.iab.MergedAppcoinsFragment.Companion.CREDITS
import com.asfoundation.wallet.util.isNoNetworkException
import com.asfoundation.wallet.wallet_blocked.WalletBlockedInteract
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3

class MergedAppcoinsPresenter(private val view: MergedAppcoinsView,
                              private val disposables: CompositeDisposable,
                              private val balanceInteract: BalanceInteract,
                              private val viewScheduler: Scheduler,
                              private val walletBlockedInteract: WalletBlockedInteract,
                              private val networkScheduler: Scheduler) {

  companion object {
    private val TAG = MergedAppcoinsFragment::class.java.simpleName
  }

  fun present(frequency: String?) {
    fetchBalance()
    handlePaymentSelectionChange(frequency != null)
    handleBuyClick()
    handleBackClick()
  }

  fun handleStop() {
    disposables.clear()
  }

  private fun fetchBalance() {
    disposables.add(Observable.zip(getAppcBalance(), getCreditsBalance(), getEthBalance(),
        Function3 { appcBalance: FiatValue, creditsBalance: FiatValue, ethBalance: FiatValue ->
          val appcFiatValue =
              FiatValue(appcBalance.amount.plus(ethBalance.amount), appcBalance.currency,
                  appcBalance.symbol)
          MergedAppcoinsBalance(appcFiatValue, creditsBalance)
        })
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnNext { view.updateBalanceValues(it.appcFiatValue, it.creditsBalance) }
        .subscribe({ }, { it.printStackTrace() }))
  }

  private fun handleBackClick() {
    disposables.add(Observable.merge(view.backClick(), view.backPressed())
        .doOnNext { view.navigateToPaymentMethods() }
        .subscribe({}, { showError(it) }))
  }

  private fun handleBuyClick() {
    disposables.add(view.buyClick()
        .doOnNext { view.showLoading() }
        .flatMapCompletable { paymentMethod ->
          walletBlockedInteract.isWalletBlocked()
              .subscribeOn(networkScheduler)
              .observeOn(viewScheduler)
              .flatMapCompletable {
                if (it) {
                  showBlockedError()
                } else {
                  handleBuyClickSelection(paymentMethod)
                }
              }
        }
        .subscribe({}, {
          view.hideLoading()
          showError(it)
        }))
  }

  private fun showBlockedError(): Completable {
    return Completable.fromAction {
      view.hideLoading()
      view.showWalletBlocked()
    }
  }

  private fun handlePaymentSelectionChange(isSubscription: Boolean) {
    disposables.add(view.getPaymentSelection()
        .doOnNext { handleSelection(it, isSubscription) }
        .subscribe({}, { showError(it) }))
  }

  private fun showError(t: Throwable) {
    t.printStackTrace()
    if (t.isNoNetworkException()) {
      view.showError(R.string.notification_no_network_poa)
    } else {
      view.showError(R.string.activity_iab_error_message)
    }
  }

  private fun handleBuyClickSelection(selection: String): Completable {
    return Completable.fromAction {
      view.hideLoading()
      when (selection) {
        APPC -> view.navigateToAppcPayment()
        CREDITS -> view.navigateToCreditsPayment()
        else -> Log.w(TAG, "No appcoins payment method selected")
      }
    }
  }

  private fun handleSelection(selection: String, isSubscription: Boolean) {
    when (selection) {
      APPC -> {
        view.hideVolatilityInfo()
        view.showBonus(isSubscription)
      }
      CREDITS -> {
        view.hideBonus()
        if (isSubscription) {
          view.showVolatilityInfo()
        }
      }
      else -> Log.w(TAG, "Error creating PublishSubject")
    }
  }

  private fun getCreditsBalance(): Observable<FiatValue> {
    return balanceInteract.getCreditsBalance()
        .map { it.second }
  }

  private fun getAppcBalance(): Observable<FiatValue> {
    return balanceInteract.getAppcBalance()
        .map { it.second }

  }

  private fun getEthBalance(): Observable<FiatValue> {
    return balanceInteract.getEthBalance()
        .map { it.second }

  }
}
