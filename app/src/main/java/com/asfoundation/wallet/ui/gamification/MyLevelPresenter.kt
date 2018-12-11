package com.asfoundation.wallet.ui.gamification

import android.os.Bundle
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.UserStats
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
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
        Single.zip(gamification.getLevels(), gamification.getUserStatus(),
            BiFunction { levels: Levels, userStats: UserStats ->
              mapToUserStatus(levels, userStats)
            })
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess {
              view.updateLevel(it)
              if (it.bonus.isNotEmpty()) view.showHowItWorksButton()
            }
            .subscribe())
  }

  private fun mapToUserStatus(levels: Levels, userStats: UserStats): UserRewardsStatus {
    var status = UserRewardsStatus()
    if (levels.status == Levels.Status.OK && userStats.status == UserStats.Status.OK) {
      val list = mutableListOf<Double>()
      for (level in levels.list) {
        if (level.bonus > 0.0) {
          list.add(level.bonus)
        }
      }
      status =
          UserRewardsStatus(userStats.level,
              userStats.totalEarned.setScale(2, RoundingMode.HALF_UP),
              list)
    }
    return status
  }

  fun stop() {
    disposables.clear()
  }

}
