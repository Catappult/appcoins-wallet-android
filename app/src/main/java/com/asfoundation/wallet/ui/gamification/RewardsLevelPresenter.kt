package com.asfoundation.wallet.ui.gamification

import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class RewardsLevelPresenter(private val activity: RewardsLevelView,
                            private val disposable: CompositeDisposable,
                            private val viewScheduler: Scheduler) {

  fun present(legacy: Boolean) {
    handleNavigation(legacy)
    handleRetryClick(legacy)
  }

  private fun handleNavigation(legacy: Boolean) {
    if (legacy) activity.loadLegacyGamificationView()
    else activity.loadGamificationView()
  }

  private fun handleRetryClick(legacy: Boolean) {
    disposable.add(activity.retryClick()
        .observeOn(viewScheduler)
        .doOnNext { activity.showRetryAnimation() }
        .delay(1, TimeUnit.SECONDS)
        .observeOn(viewScheduler)
        .doOnNext { handleNavigation(legacy) }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() = disposable.clear()
}
