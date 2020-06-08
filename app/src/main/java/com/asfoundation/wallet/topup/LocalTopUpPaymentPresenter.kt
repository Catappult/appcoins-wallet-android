package com.asfoundation.wallet.topup

import com.asfoundation.wallet.ui.iab.LocalPaymentInteractor
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class LocalTopUpPaymentPresenter(private val view: LocalTopUpPaymentFragment,
                                 private val activityView: TopUpActivityView,
                                 private val interactor: LocalPaymentInteractor,
                                 private val viewScheduler: Scheduler,
                                 private val networkScheduler: Scheduler,
                                 private val disposables: CompositeDisposable,
                                 private val data: TopUpPaymentData,
                                 private val paymentId: String) {

  fun present() {

  }

  fun stop() = disposables.clear()
}
