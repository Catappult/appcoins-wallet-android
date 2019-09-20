package com.asfoundation.wallet.ui.gamification

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class RewardsLevelPresenter(private val activity: RewardsLevelActivity,
                            private val disposable: CompositeDisposable,
                            private val viewScheduler: Scheduler) {

  fun present() {
    handleRetryClick()
  }

  private fun handleRetryClick() {
    disposable.add(activity.retryClick()
        .observeOn(viewScheduler)
        .doOnNext { activity.showRetryAnimation() }
        .delay(1, TimeUnit.SECONDS)
        .doOnNext { activity.loadMyLevelFragment() }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() {
    disposable.clear()
  }
}
