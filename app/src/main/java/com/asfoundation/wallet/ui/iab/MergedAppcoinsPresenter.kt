package com.asfoundation.wallet.ui.iab

import android.util.Log
import com.asf.wallet.R
import com.asfoundation.wallet.entity.TransactionBuilder
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

  fun present() {
    setupUi()
  }

  private fun setupUi() {
  }

  private fun map(payments: List<PaymentMethod>): Pair<PaymentMethod, PaymentMethod> {
    for (payment: PaymentMethod in payments) {
      Log.d("TAG123", payment.label)
    }
    return Pair(payments[0], payments[1])
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
}
