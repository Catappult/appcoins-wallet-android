package com.asfoundation.wallet.ui.iab

import android.util.Log
import com.asf.wallet.R
import com.asfoundation.wallet.entity.TransactionBuilder
import com.asfoundation.wallet.ui.iab.MergedAppcoinsFragment.Companion.APPC
import com.asfoundation.wallet.ui.iab.MergedAppcoinsFragment.Companion.CREDITS
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.io.IOException

class MergedAppcoinsPresenter(private val view: MergedAppcoinsView,
                              private val transaction: TransactionBuilder,
                              private val fiatAmount: String, private val currency: String,
                              private val inAppPurchaseInteractor: InAppPurchaseInteractor,
                              private val disposables: CompositeDisposable,
                              private val viewScheduler: Scheduler,
                              private val networkScheduler: Scheduler) {

  companion object {
    private val TAG = MergedAppcoinsFragment::class.java.simpleName
  }

  fun present() {
    handlePaymentSelectionChange()
    handleBuyClick()
    handleBackClick()
  }

  private fun handleBackClick() {
    disposables.add(Observable.merge(view.backClick(), view.backPressed())
        .doOnNext {
          view.navigateToPaymentMethods(PaymentMethodsView.SelectedPaymentMethod.MERGED_APPC)
        }
        .subscribe({}, { showError(it) }))
  }

  private fun handleBuyClick() {
    disposables.add(view.buyClick()
        .doOnNext { handleBuyClickSelection(it) }
        .subscribe({}, { showError(it) }))
  }

  private fun handlePaymentSelectionChange() {
    disposables.add(view.getPaymentSelection()
        .doOnNext { handleSelection(it) }
        .subscribe({}, { showError(it) }))
  }


  private fun showError(t: Throwable) {
    t.printStackTrace()
    if (isNoNetworkException(t)) {
      view.showError(R.string.notification_no_network_poa)
    } else {
      view.showError(R.string.activity_iab_error_message)
    }
  }

  private fun isNoNetworkException(throwable: Throwable): Boolean {
    return throwable is IOException || throwable.cause != null && throwable.cause is IOException
  }

  private fun handleBuyClickSelection(selection: String) {
    when (selection) {
      APPC -> view.navigateToAppcPayment()
      CREDITS -> view.navigateToCreditsPayment()
      else -> Log.w(TAG, "No appcoins payment method selected")
    }
  }

  private fun handleSelection(selection: String) {
    when (selection) {
      APPC -> view.showBonus()
      CREDITS -> view.hideBonus()
      else -> Log.w(Companion.TAG, "Error creating PublishSubject")
    }
  }
}
