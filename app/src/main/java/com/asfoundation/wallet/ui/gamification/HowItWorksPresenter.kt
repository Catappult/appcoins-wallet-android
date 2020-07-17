package com.asfoundation.wallet.ui.gamification

import android.os.Bundle
import com.appcoins.wallet.gamification.repository.GamificationStats
import com.appcoins.wallet.gamification.repository.Levels
import com.asfoundation.wallet.analytics.gamification.GamificationAnalytics
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.util.isNoNetworkException
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.math.BigDecimal
import java.math.RoundingMode

class HowItWorksPresenter(private val view: HowItWorksView,
                          private val activityView: GamificationActivityView,
                          private val gamification: GamificationInteractor,
                          private val analytics: GamificationAnalytics,
                          private val disposables: CompositeDisposable,
                          private val networkScheduler: Scheduler,
                          private val viewScheduler: Scheduler,
                          private val formatter: CurrencyFormatUtils) {

  fun present(savedInstanceState: Bundle?) {
    handleShowLevels()
    handleShowNextLevelFooter()
    handleBottomSheetHeaderClick()
    if (savedInstanceState == null) {
      sendEvent()
    }
  }

  private fun handleBottomSheetHeaderClick() {
    disposables.add(view.bottomSheetHeaderClick()
        .doOnNext { view.changeBottomSheetState() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleShowLevels() {
    disposables.add(
        Single.zip(gamification.getLevels(), gamification.getUserStats(),
            BiFunction { levels: Levels, gamificationStats: GamificationStats ->
              handlePeekInformation(gamificationStats.totalEarned, gamificationStats.totalSpend,
                  gamificationStats.status)
              mapToViewLevels(levels, gamificationStats)
            })
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess {
              if (it.status == Status.NO_NETWORK) {
                activityView.showNetworkErrorView()
              } else {
                activityView.showMainView()
                view.showLevels(it.list, it.level, it.updateDate)
              }
            }
            .subscribe({ }, { handleError(it) }))
  }

  private fun handlePeekInformation(totalEarned: BigDecimal, totalSpend: BigDecimal,
                                    status: GamificationStats.Status) {
    if (status == GamificationStats.Status.OK) {
      disposables.add(gamification.getAppcToLocalFiat(totalEarned.toString(), 2)
          .filter { it.amount.toInt() >= 0 }
          .observeOn(viewScheduler)
          .doOnNext {
            val totalSpent = formatter.formatCurrency(totalSpend, WalletCurrency.FIAT)
            val bonusEarned = formatter.formatCurrency(it.amount, WalletCurrency.FIAT)
            view.showPeekInformation(totalSpent, bonusEarned, it.symbol)
          }
          .subscribeOn(networkScheduler)
          .subscribe({}, { handleError(it) }))
    }
  }

  private fun handleShowNextLevelFooter() {
    disposables.add(
        Single.zip(gamification.getLevels(), gamification.getUserStats(),
            BiFunction { levels: Levels, gamificationStats: GamificationStats ->
              mapToUserStatus(levels, gamificationStats)
            })
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess {
              if (it.status == Status.NO_NETWORK) {
                activityView.showNetworkErrorView()
              } else {
                activityView.showMainView()
                view.showNextLevelFooter(it)
              }
            }
            .subscribe({ }, { handleError(it) }))
  }

  private fun mapToViewLevels(levels: Levels, gamificationStats: GamificationStats): ViewLevels {
    val list = mutableListOf<ViewLevel>()
    var status = Status.OK
    if (levels.status == Levels.Status.OK && gamificationStats.status == GamificationStats.Status.OK) {
      for (level in levels.list) {
        list.add(ViewLevel(level.level, level.amount, level.bonus,
            gamificationStats.totalSpend >= level.amount))
      }
    }
    if (levels.status == Levels.Status.NO_NETWORK || gamificationStats.status == GamificationStats.Status.NO_NETWORK) {
      status = Status.NO_NETWORK
    }
    return ViewLevels(list.toList(), gamificationStats.level, status, levels.updateDate)
  }

  private fun mapToUserStatus(levels: Levels,
                              gamificationStats: GamificationStats): UserRewardsStatus {
    var status = Status.OK
    if (levels.status == Levels.Status.OK && gamificationStats.status == GamificationStats.Status.OK) {
      val nextLevelAmount = gamificationStats.nextLevelAmount?.minus(
          gamificationStats.totalSpend)
          ?.setScale(2, RoundingMode.HALF_UP) ?: BigDecimal.ZERO
      return UserRewardsStatus(level = gamificationStats.level, toNextLevelAmount = nextLevelAmount,
          status = status)
    }
    if (levels.status == Levels.Status.NO_NETWORK || gamificationStats.status == GamificationStats.Status.NO_NETWORK) {
      status = Status.NO_NETWORK
    }
    return UserRewardsStatus(status = status)
  }

  private fun sendEvent() {
    disposables.add(gamification.getUserStats()
        .subscribeOn(networkScheduler)
        .doOnSuccess { analytics.sendMoreInfoScreenViewEvent(it.level + 1) }
        .subscribe())
  }

  private fun handleError(throwable: Throwable) {
    throwable.printStackTrace()
    if (throwable.isNoNetworkException()) {
      activityView.showNetworkErrorView()
    }
  }

  fun stop() {
    disposables.clear()
  }
}
