package com.asfoundation.wallet.ui.gamification

import android.os.Bundle
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.UserStats
import com.asfoundation.wallet.analytics.gamification.GamificationAnalytics
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.math.BigDecimal
import java.math.RoundingMode

class HowItWorksPresenter(private val view: HowItWorksView,
                          private val gamification: GamificationInteractor,
                          private val analytics: GamificationAnalytics,
                          private val networkScheduler: Scheduler,
                          private val viewScheduler: Scheduler) {

  val disposables = CompositeDisposable()

  fun present(savedInstanceState: Bundle?) {
    handleShowLevels()
    handleShowPeekInformation()
    handleShowNextLevelFooter()
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
            .doOnSuccess { view.showLevels(it.first, it.second) }
            .subscribe())
  }

  private fun handleShowPeekInformation() {
    disposables.add(gamification.getUserStatus()
        .flatMapObservable { userStats ->
          gamification.getAppcToLocalFiat(userStats.totalEarned.toString(), 2)
              .filter { it.amount.toInt() >= 0 }
              .observeOn(viewScheduler)
              .doOnNext { view.showPeekInformation(userStats, it) }
        }
        .subscribeOn(networkScheduler)
        .subscribe())
  }

  private fun handleShowNextLevelFooter() {
    disposables.add(
        Single.zip(gamification.getLevels(), gamification.getUserStatus(),
            BiFunction { levels: Levels, userStats: UserStats ->
              mapToUserStatus(levels, userStats)
            })
            .observeOn(viewScheduler)
            .doOnSuccess { view.showNextLevelFooter(it) }
            .subscribeOn(networkScheduler)
            .subscribe())
  }

  private fun mapToViewLevels(levels: Levels, userStats: UserStats): Pair<List<ViewLevel>, Int> {
    val list = mutableListOf<ViewLevel>()
    if (levels.status == Levels.Status.OK && userStats.status == UserStats.Status.OK) {
      for (level in levels.list) {
        list.add(
            ViewLevel(level.level, level.amount, level.bonus,
                userStats.totalSpend >= level.amount))
      }
    }
    return Pair(list.toList(), userStats.level)
  }

  private fun mapToUserStatus(levels: Levels, userStats: UserStats): UserRewardsStatus {
    if (levels.status == Levels.Status.OK && userStats.status == UserStats.Status.OK) {
      val nextLevelAmount = userStats.nextLevelAmount?.minus(
          userStats.totalSpend)?.setScale(2, RoundingMode.HALF_UP) ?: BigDecimal.ZERO
      return UserRewardsStatus(level = userStats.level, toNextLevelAmount = nextLevelAmount)
    }
    return UserRewardsStatus()
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
