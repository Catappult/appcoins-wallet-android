package com.asfoundation.wallet.ui.gamification

import android.os.Bundle
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.UserStats
import com.asfoundation.wallet.analytics.gamification.GamificationAnalytics
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import java.math.BigDecimal
import java.math.RoundingMode

class MyLevelPresenter(private val view: MyLevelView,
                       private val gamification: GamificationInteractor,
                       private val analytics: GamificationAnalytics,
                       private val networkScheduler: Scheduler,
                       private val viewScheduler: Scheduler) {
  val disposables = CompositeDisposable()
  fun present(savedInstanceState: Bundle?) {
    handleShowLevels(savedInstanceState == null)
    handleButtonClick()
    view.setupLayout()
  }

  private fun handleButtonClick() {
    disposables.add(view.getButtonClicks().doOnNext { view.showHowItWorksScreen() }.subscribe())
  }

  private fun handleShowLevels(sendEvent: Boolean) {
    disposables.add(
        Single.zip(gamification.getLevels(), gamification.getUserStatus(),
            gamification.getLastShownLevel(),
            Function3 { levels: Levels, userStats: UserStats, lastShownLevel: Int ->
              mapToUserStatus(levels, userStats, lastShownLevel)
            })
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess {
              if (it.lastShownLevel > 0 || it.lastShownLevel == 0 && it.level == 0) {
                view.setStaringLevel(it)
              }
              view.updateLevel(it)
              if (sendEvent) {
                analytics.sendMainScreenViewEvent(it.level + 1)
              }
              if (it.bonus.isNotEmpty()) view.showHowItWorksButton()
            }
            .flatMapCompletable { gamification.levelShown(it.level) }
            .subscribe())
  }

  private fun mapToUserStatus(levels: Levels, userStats: UserStats,
                              lastShownLevel: Int): UserRewardsStatus {
    if (levels.status == Levels.Status.OK && userStats.status == UserStats.Status.OK) {
      val list = mutableListOf<Double>()
      if (levels.isActive) {
        for (level in levels.list) {
          list.add(level.bonus)
        }
      }
      val nextLevelAmount = userStats.nextLevelAmount?.minus(
          userStats.totalSpend)?.setScale(2, RoundingMode.HALF_UP) ?: BigDecimal.ZERO
      return UserRewardsStatus(lastShownLevel, userStats.level, nextLevelAmount, list)
    }
    return UserRewardsStatus(lastShownLevel)
  }

  fun stop() {
    disposables.clear()
  }



}
