package com.asfoundation.wallet.ui.iab

import android.util.Log
import com.asf.wallet.R
import com.asfoundation.wallet.billing.analytics.BillingAnalytics
import com.asfoundation.wallet.repository.PreferencesRepositoryType
import com.asfoundation.wallet.ui.iab.MergedAppcoinsFragment.Companion.APPC
import com.asfoundation.wallet.ui.iab.MergedAppcoinsFragment.Companion.CREDITS
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.util.isNoNetworkException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import java.util.concurrent.TimeUnit

class MergedAppcoinsPresenter(private val view: MergedAppcoinsView,
                              private val activityView: IabView,
                              private val disposables: CompositeDisposable,
                              private val viewScheduler: Scheduler,
                              private val networkScheduler: Scheduler,
                              private val analytics: BillingAnalytics,
                              private val formatter: CurrencyFormatUtils,
                              private val mergedAppcoinsInteract: MergedAppcoinsInteract,
                              private val gamificationLevel: Int,
                              private val navigator: Navigator,
                              private val preferencesRepositoryType: PreferencesRepositoryType) {

  companion object {
    private val TAG = MergedAppcoinsFragment::class.java.simpleName
  }

  fun present() {
    fetchBalance()
    handlePaymentSelectionChange()
    handleBuyClick()
    handleBackClick()
    handleSupportClicks()
    handleErrorDismiss()
    handleAuthenticationResult()
  }

  fun handleStop() = disposables.clear()

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
        .doOnNext {
          val appcFiat = formatter.formatCurrency(it.appcFiatValue.amount, WalletCurrency.APPCOINS)
          val creditsFiat = formatter.formatCurrency(it.creditsBalance.amount,
              WalletCurrency.CREDITS)
          view.updateBalanceValues(appcFiat, creditsFiat, it.creditsBalance.currency)
        }
        .subscribe({ }, { it.printStackTrace() }))
  }

  private fun handleBackClick() {
    disposables.add(Observable.merge(view.backClick(), view.backPressed())
        .observeOn(networkScheduler)
        .doOnNext { paymentMethod ->
          analytics.sendPaymentConfirmationEvent(paymentMethod.packageName,
              paymentMethod.skuDetails, paymentMethod.value, paymentMethod.purchaseDetails,
              paymentMethod.transactionType, "cancel")
        }
        .observeOn(viewScheduler)
        .doOnNext { activityView.showPaymentMethodsView() }
        .subscribe({}, { showError(it) }))
  }

  private fun handleAuthenticationResult() {
    disposables.add(activityView.onAuthenticationResult()
        .observeOn(viewScheduler)
        .doOnNext {
          if (!it.isSuccess) view.hideLoading()
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleBuyClick() {
    disposables.add(view.buyClick()
        .observeOn(networkScheduler)
        .doOnNext { paymentMethod ->
          analytics.sendPaymentConfirmationEvent(paymentMethod.packageName,
              paymentMethod.skuDetails, paymentMethod.value, paymentMethod.purchaseDetails,
              paymentMethod.transactionType, "buy")
        }
        .observeOn(viewScheduler)
        .doOnNext {
          view.showLoading()
        }
        .flatMapSingle { paymentMethod ->
          mergedAppcoinsInteract.isWalletBlocked()
              .subscribeOn(networkScheduler)
              .observeOn(viewScheduler)
              .doOnSuccess {
                if (preferencesRepositoryType.hasAuthenticationPermission()) {
                  view.showAuthenticationActivity(map(paymentMethod.purchaseDetails),
                      gamificationLevel)
                } else {
                  handleBuyClickSelection(paymentMethod.purchaseDetails)
                }
              }

        }
        .subscribe({}, {
          view.hideLoading()
          showError(it)
        }))
  }

  private fun map(purchaseDetails: String): String {
    if (purchaseDetails == APPC) return PaymentMethodsView.PaymentMethodId.APPC.id
    else if (purchaseDetails == CREDITS) return PaymentMethodsView.PaymentMethodId.APPC_CREDITS.id
    return ""
  }

  private fun handleSupportClicks() {
    disposables.add(Observable.merge(view.getSupportIconClicks(), view.getSupportLogoClicks())
        .throttleFirst(50, TimeUnit.MILLISECONDS)
        .flatMapCompletable { mergedAppcoinsInteract.showSupport(gamificationLevel) }
        .subscribe()
    )
  }

  private fun handleErrorDismiss() {
    disposables.add(view.errorDismisses()
        .observeOn(viewScheduler)
        .doOnNext { navigator.popViewWithError() }
        .subscribe())
  }

  private fun handlePaymentSelectionChange() {
    disposables.add(view.getPaymentSelection()
        .doOnNext { handleSelection(it) }
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

  private fun handleSelection(selection: String) {
    when (selection) {
      APPC -> view.showBonus()
      CREDITS -> view.hideBonus()
      else -> Log.w(TAG, "Error creating PublishSubject")
    }
  }

  private fun getCreditsBalance(): Observable<FiatValue> =
      mergedAppcoinsInteract.getCreditsBalance()

  private fun getAppcBalance(): Observable<FiatValue> = mergedAppcoinsInteract.getAppcBalance()

  private fun getEthBalance(): Observable<FiatValue> = mergedAppcoinsInteract.getEthBalance()
}
