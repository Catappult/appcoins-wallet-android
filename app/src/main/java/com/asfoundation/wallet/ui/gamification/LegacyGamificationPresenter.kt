package com.asfoundation.wallet.ui.gamification

import android.os.Bundle
import com.appcoins.wallet.gamification.GamificationScreen
import com.appcoins.wallet.gamification.repository.GamificationStats
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.UserType
import com.asfoundation.wallet.analytics.gamification.GamificationAnalytics
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import java.math.BigDecimal
import java.math.RoundingMode

class LegacyGamificationPresenter(private val view: LegacyGamificationView,
                                  private val activityView: GamificationActivityView,
                                  private val gamification: GamificationInteractor,
                                  private val analytics: GamificationAnalytics,
                                  private val disposables: CompositeDisposable,
                                  private val networkScheduler: Scheduler,
                                  private val viewScheduler: Scheduler) {

  fun present(savedInstanceState: Bundle?) {
    handleLevelInformation(savedInstanceState == null)
    handleInfoButtonClick()
    view.animateBackgroundFade()
  }

  private fun handleLevelInformation(sendEvent: Boolean) {
    disposables.add(Single.zip(gamification.getLevels(), gamification.getUserStats(),
        gamification.getLastShownLevel(GamificationScreen.MY_LEVEL),
        Function3 { levels: Levels, gamificationStats: GamificationStats, lastShownLevel: Int ->
          mapToUserStatus(levels, gamificationStats, lastShownLevel)
        })
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess {
          displayInformation(it.status, it.lastShownLevel, it.level, it.bonus, sendEvent,
              it.userType)
        }
        .flatMapCompletable { gamification.levelShown(it.level, GamificationScreen.MY_LEVEL) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun displayInformation(status: Status, lastShownLevel: Int, level: Int,
                                 bonus: List<Double>, sendEvent: Boolean, userType: UserType) {
    if (status == Status.NO_NETWORK) {
      activityView.showNetworkErrorView()
    } else {
      activityView.showMainView()
      view.showPioneerUser()
      view.setLevelIcons()
      if (lastShownLevel > 0 || lastShownLevel == 0 && level == 0) {
        view.setStaringLevel(lastShownLevel, level, bonus)
      }
      view.updateLevel(lastShownLevel, level, bonus)
      if (sendEvent) {
        analytics.sendMainScreenViewEvent(level + 1)
      }
    }
  }

  private fun handleInfoButtonClick() {
    disposables.add(activityView.getInfoButtonClick()
        .doOnNext { view.changeBottomSheetState() }
        .subscribe())
  }

  private fun mapToUserStatus(levels: Levels, gamificationStats: GamificationStats,
                              lastShownLevel: Int): UserRewardsStatus {
    var status = Status.OK
    if (levels.status == Levels.Status.OK && gamificationStats.status == GamificationStats.Status.OK) {
      val list = mutableListOf<Double>()
      if (levels.isActive) {
        for (level in levels.list) {
          list.add(level.bonus)
        }
      }
      val nextLevelAmount = gamificationStats.nextLevelAmount?.minus(gamificationStats.totalSpend)
          ?.setScale(2, RoundingMode.HALF_UP) ?: BigDecimal.ZERO
      return UserRewardsStatus(lastShownLevel, gamificationStats.level, nextLevelAmount, list,
          status, userType = gamificationStats.userType)
    }
    if (levels.status == Levels.Status.NO_NETWORK || gamificationStats.status == GamificationStats.Status.NO_NETWORK) {
      status = Status.NO_NETWORK
    }
    return UserRewardsStatus(lastShownLevel, lastShownLevel, status = status)
  }

  fun stop() = disposables.clear()
}
