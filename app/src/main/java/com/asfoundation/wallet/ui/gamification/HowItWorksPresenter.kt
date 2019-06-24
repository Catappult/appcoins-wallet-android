package com.asfoundation.wallet.ui.gamification

import android.os.Bundle
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.UserStats
import com.asfoundation.wallet.analytics.gamification.GamificationAnalytics
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction

class HowItWorksPresenter(private val view: HowItWorksView,
                          private val gamification: GamificationInteractor,
                          private val analytics: GamificationAnalytics,
                          private val networkScheduler: Scheduler,
                          private val viewScheduler: Scheduler) {
  val disposables = CompositeDisposable()
  fun present(savedInstanceState: Bundle?) {
    handleOkClick()
    handleShowLevels()
    if (savedInstanceState == null) {
      sendEvent()
    }
  }

  private fun handleShowLevels() {
    disposables.add(
        Single.zip(gamification.getLevels(), gamification.getUserStatus(),
            BiFunction { levels: Levels, userStats: UserStats ->
              mapToViewLevels(levels, userStats)
            })
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess {
              view.showLevels(it)
            }
            .subscribe())
  }

  private fun mapToViewLevels(levels: Levels, userStats: UserStats): List<ViewLevel> {
    val list = mutableListOf<ViewLevel>()
    if (levels.status == Levels.Status.OK && userStats.status == UserStats.Status.OK) {
      for (level in levels.list) {
        list.add(
            ViewLevel(level.level, level.amount, level.bonus, userStats.totalSpend >= level.amount))
      }
    }
    return list.toList()
  }

  private fun handleOkClick() {
    disposables.add(view.getOkClick().doOnNext { view.close() }.subscribe())
  }

  private fun sendEvent() {
    disposables.add(gamification.getUserStatus().subscribeOn(networkScheduler).doOnSuccess {
      analytics.sendMoreInfoScreenViewEvent(it.level + 1)
    }.subscribe())
  }

  fun stop() {
    disposables.clear()
  }

}
