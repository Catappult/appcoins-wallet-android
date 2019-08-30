package com.asfoundation.wallet.referrals

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class ReferralsPresenter(private val view: ReferralsView,
                         private val referralInteractor: ReferralInteractorContract,
                         private val disposable: CompositeDisposable,
                         private val viewScheduler: Scheduler,
                         private val networkScheduler: Scheduler) {

  fun present() {
    handleReferralInformation()
  }

  private fun handleReferralInformation() {
    disposable.add(referralInteractor.retrieveReferral()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          view.setupLayout(it.completed, it.currency, it.receivedAmount, it.amount, it.maxAmount,
              it.available)
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() {
    disposable.clear()
  }
}
