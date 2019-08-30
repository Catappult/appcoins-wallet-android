package com.asfoundation.wallet.referrals

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable

class ReferralsPresenter(private val view: ReferralsView,
                         private val disposable: CompositeDisposable,
                         private val viewScheduler: Scheduler,
                         private val networkScheduler: Scheduler) {

  fun present() {

  }

  fun stop() {
    disposable.clear()
  }
}
