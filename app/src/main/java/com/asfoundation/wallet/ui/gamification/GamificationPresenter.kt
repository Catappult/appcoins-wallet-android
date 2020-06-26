package com.asfoundation.wallet.ui.gamification

import android.os.Bundle
import com.appcoins.wallet.gamification.GamificationScreen
import com.appcoins.wallet.gamification.repository.GamificationStats
import com.appcoins.wallet.gamification.repository.Levels
import com.asfoundation.wallet.analytics.gamification.GamificationAnalytics
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction

class GamificationPresenter(private val view: GamificationFragment,
                            private val activityView: RewardsLevelView,
                            private val gamification: GamificationInteractor,
                            private val analytics: GamificationAnalytics,
                            private val disposables: CompositeDisposable,
                            private val viewScheduler: Scheduler,
                            private val networkScheduler: Scheduler) {

  fun present(savedInstanceState: Bundle?) {
    handleLevelInformation(savedInstanceState == null)
  }

  private fun handleLevelInformation(sendEvent: Boolean) {
    disposables.add(Single.zip(gamification.getLevels(), gamification.getUserStats(),
        BiFunction { levels: Levels, gamificationStats: GamificationStats ->
          mapToUserStatus(levels, gamificationStats)
        })
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { displayInformation(it, sendEvent) }
        .flatMapCompletable {
          gamification.levelShown(it.currentLevel, GamificationScreen.MY_LEVEL)
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun mapToUserStatus(levels: Levels,
                              gamificationStats: GamificationStats): GamificationInfo {
    var status = Status.UNKNOWN_ERROR
    if (levels.status == Levels.Status.OK && gamificationStats.status == GamificationStats.Status.OK) {
      return GamificationInfo(gamificationStats.level, gamificationStats.totalSpend, levels.list,
          Status.OK)
    }
    if (levels.status == Levels.Status.NO_NETWORK || gamificationStats.status == GamificationStats.Status.NO_NETWORK) {
      status = Status.NO_NETWORK
    }
    return GamificationInfo(status)
  }

  private fun displayInformation(gamification: GamificationInfo, sendEvent: Boolean) {
    if (gamification.status != Status.OK) {
      activityView.showNetworkErrorView()
    } else {
      val currentLevel = gamification.currentLevel
      activityView.showMainView()
      view.displayGamificationInfo(currentLevel, gamification.levels, gamification.totalSpend)
      if (sendEvent) analytics.sendMainScreenViewEvent(currentLevel + 1)
    }
  }

  fun stop() = disposables.clear()
}
