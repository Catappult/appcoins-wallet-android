package com.asfoundation.wallet.ui.iab

import android.os.Bundle
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class PaymentMethodsPresenter(
    private val view: PaymentMethodsView,
    private val viewScheduler: Scheduler,
    private val networkThread: Scheduler,
    private val disposables: CompositeDisposable,
    private val inAppPurchaseInteractor: InAppPurchaseInteractor) {

  fun present(savedInstanceState: Bundle?) {
    if (savedInstanceState == null) {
      disposables.add(inAppPurchaseInteractor.paymentMethods.subscribeOn(networkThread).flatMap {
        Observable.fromIterable(it).map { paymentMethod ->
          PaymentMethod(paymentMethod.id, paymentMethod.label, paymentMethod.iconUrl, true)
        }.toList()

      }.observeOn(viewScheduler)
          .subscribe({ view.showPaymentMethods(it) },
              {
                it.printStackTrace()
                view.showError()
              }))
    }
  }

  fun stop() {
    disposables.clear()
  }

}
