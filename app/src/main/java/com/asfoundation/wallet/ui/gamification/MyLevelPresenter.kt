package com.asfoundation.wallet.ui.gamification

import android.os.Bundle
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.UserStats
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import java.math.BigDecimal
import java.math.RoundingMode

class MyLevelPresenter(private val view: MyLevelView,
                       private val gamification: GamificationInteractor,
                       private val networkScheduler: Scheduler,
                       private val viewScheduler: Scheduler) {
  val disposables = CompositeDisposable()
  fun present(savedInstanceState: Bundle?) {
    handleShowLevels()
    handleButtonClick()
    view.setupLayout()
  }

  private fun handleButtonClick() {
    disposables.add(view.getButtonClicks().doOnNext { view.showHowItWorksScreen() }.subscribe())
  }

  private fun handleShowLevels() {
    disposables.add(
        Single.zip(gamification.getLevels(), gamification.getUserStatus(), gamification.hasNewLevel(),
            Function3 { levels: Levels, userStats: UserStats, hasNewLevel: Boolean ->
              mapToUserStatus(levels, userStats, hasNewLevel)
            })
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess {
              view.updateLevel(it, it.hasNewLevel)
              if (it.bonus.isNotEmpty()) view.showHowItWorksButton()
            }
            .flatMapCompletable { gamification.levelShown(it.level) }
            .subscribe())
  }

  private fun mapToUserStatus(levels: Levels, userStats: UserStats, hasNewLevel: Boolean): UserRewardsStatus {
    var status = UserRewardsStatus()
    if (levels.status == Levels.Status.OK && userStats.status == UserStats.Status.OK) {
      val list = mutableListOf<Double>()
      if (levels.isActive) {
        for (level in levels.list) {
          list.add(level.bonus)
        }
      }
      val nextLevelAmount = userStats.nextLevelAmount?.minus(
          userStats.totalSpend)?.setScale(2, RoundingMode.HALF_UP) ?: BigDecimal.ZERO
      status =
          UserRewardsStatus(userStats.level, nextLevelAmount, list, hasNewLevel)
    }
    return status
  }

  fun stop() {
    disposables.clear()
  }

}
