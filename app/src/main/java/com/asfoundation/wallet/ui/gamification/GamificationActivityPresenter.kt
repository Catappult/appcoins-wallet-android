package com.asfoundation.wallet.ui.gamification

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class GamificationActivityPresenter(private val activity: GamificationActivityView,
                                    private val disposable: CompositeDisposable,
                                    private val viewScheduler: Scheduler) {

  fun present() {
    activity.loadGamificationView()
    handleRetryClick()
  }

  private fun handleRetryClick() {
    disposable.add(activity.retryClick()
        .observeOn(viewScheduler)
        .doOnNext { activity.showRetryAnimation() }
        .delay(1, TimeUnit.SECONDS)
        .observeOn(viewScheduler)
        .doOnNext { activity.loadGamificationView() }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() = disposable.clear()
}
