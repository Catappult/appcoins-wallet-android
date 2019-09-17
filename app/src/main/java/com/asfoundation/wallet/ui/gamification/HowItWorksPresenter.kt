package com.asfoundation.wallet.ui.gamification

import android.os.Bundle
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.UserStats
import com.asfoundation.wallet.analytics.gamification.GamificationAnalytics
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode

class HowItWorksPresenter(private val view: HowItWorksView,
                          private val activity: GamificationView?,
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
        Single.zip(gamification.getLevels(), gamification.getUserStats(),
            BiFunction { levels: Levels, userStats: UserStats ->
              mapToViewLevels(levels, userStats)
            })
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess {
              if (it.third == Status.NO_NETWORK) {
                activity?.showNetworkErrorView()
              } else {
                activity?.showMainView()
                view.showLevels(it.first, it.second)
              }
            }
            .subscribe({ }, { handleError(it) }))
  }

  private fun handleShowPeekInformation() {
    disposables.add(gamification.getUserStats()
        .flatMapObservable { userStats ->
          gamification.getAppcToLocalFiat(userStats.totalEarned.toString(), 2)
              .filter { it.amount.toInt() >= 0 }
              .observeOn(viewScheduler)
              .doOnNext { view.showPeekInformation(userStats, it) }
        }
        .subscribeOn(networkScheduler)
        .subscribe({}, { handleError(it) }))
  }

  private fun handleShowNextLevelFooter() {
    disposables.add(
        Single.zip(gamification.getLevels(), gamification.getUserStats(),
            BiFunction { levels: Levels, userStats: UserStats ->
              mapToUserStatus(levels, userStats)
            })
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess {
              if (it.status == Status.NO_NETWORK) {
                activity?.showNetworkErrorView()
              } else {
                activity?.showMainView()
                view.showNextLevelFooter(it)
              }
            }
            .subscribe({ }, { handleError(it) }))
  }

  private fun mapToViewLevels(levels: Levels,
                              userStats: UserStats): Triple<List<ViewLevel>, Int, Status> {
    val list = mutableListOf<ViewLevel>()
    var status = Status.OK
    if (levels.status == Levels.Status.OK && userStats.status == UserStats.Status.OK) {
      for (level in levels.list) {
        list.add(
            ViewLevel(level.level, level.amount, level.bonus,
                userStats.totalSpend >= level.amount))
      }
    }
    if (levels.status == Levels.Status.NO_NETWORK || userStats.status == UserStats.Status.NO_NETWORK) {
      status = Status.NO_NETWORK
    }
    return Triple(list.toList(), userStats.level, status)
  }

  private fun mapToUserStatus(levels: Levels, userStats: UserStats): UserRewardsStatus {
    var status = Status.OK
    if (levels.status == Levels.Status.OK && userStats.status == UserStats.Status.OK) {
      val nextLevelAmount = userStats.nextLevelAmount?.minus(
          userStats.totalSpend)?.setScale(2, RoundingMode.HALF_UP) ?: BigDecimal.ZERO
      return UserRewardsStatus(level = userStats.level, toNextLevelAmount = nextLevelAmount,
          status = status)
    }
    if (levels.status == Levels.Status.NO_NETWORK || userStats.status == UserStats.Status.NO_NETWORK) {
      status = Status.NO_NETWORK
    }
    return UserRewardsStatus(status = status)
  }

  private fun sendEvent() {
    disposables.add(gamification.getUserStats().subscribeOn(networkScheduler)
        .doOnSuccess { analytics.sendMoreInfoScreenViewEvent(it.level + 1) }
        .subscribe())
  }

  private fun handleError(throwable: Throwable) {
    throwable.printStackTrace()
    if (isNoNetworkException(throwable)) {
      activity?.showNetworkErrorView()
    }
  }

  private fun isNoNetworkException(throwable: Throwable): Boolean {
    return throwable is IOException || throwable.cause != null && throwable.cause is IOException
  }


  fun stop() {
    disposables.clear()
  }
}
