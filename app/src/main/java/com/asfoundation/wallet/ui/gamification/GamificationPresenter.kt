package com.asfoundation.wallet.ui.gamification

import android.os.Bundle
import com.appcoins.wallet.gamification.GamificationContext
import com.appcoins.wallet.gamification.LevelModel
import com.appcoins.wallet.gamification.LevelModel.LevelType
import com.appcoins.wallet.gamification.repository.GamificationStats
import com.appcoins.wallet.gamification.repository.Levels
import com.asfoundation.wallet.analytics.gamification.GamificationAnalytics
import com.asfoundation.wallet.ui.gamification.GamificationFragment.Companion.GAMIFICATION_INFO_ID
import com.asfoundation.wallet.ui.gamification.GamificationFragment.Companion.SHOW_REACHED_LEVELS_ID
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.util.isNoNetworkException
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.math.BigDecimal

class GamificationPresenter(private val view: GamificationView,
                            private val activityView: GamificationActivityView,
                            private val gamification: GamificationInteractor,
                            private val analytics: GamificationAnalytics,
                            private val formatter: CurrencyFormatUtils,
                            private val disposables: CompositeDisposable,
                            private val viewScheduler: Scheduler,
                            private val networkScheduler: Scheduler) {

  fun present(savedInstanceState: Bundle?) {
    handleLevelInformation(savedInstanceState == null)
    handleLevelsClick()
    handleBottomSheetVisibility()
    handleBackPress()
  }

  private fun handleLevelsClick() {
    disposables.add(view.getUiClick()
        .doOnNext {
          when (it.first) {
            SHOW_REACHED_LEVELS_ID -> view.toggleReachedLevels(it.second)
            GAMIFICATION_INFO_ID -> view.updateBottomSheetVisibility()
          }
        }
        .subscribe())
  }

  private fun handleLevelInformation(sendEvent: Boolean) {
    disposables.add(Single.zip(gamification.getLevels(), gamification.getUserStats(),
        BiFunction { levels: Levels, gamificationStats: GamificationStats ->
          handleHeaderInformation(gamificationStats.totalEarned, gamificationStats.totalSpend,
              gamificationStats.status)
          mapToGamificationInfo(levels, gamificationStats)
        })
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { displayInformation(it, sendEvent) }
        .flatMapCompletable {
          gamification.levelShown(it.currentLevel, GamificationContext.SCREEN_MY_LEVEL)
        }
        .subscribe({}, { handleError(it) }))
  }

  private fun mapToGamificationInfo(levels: Levels,
                                    gamificationStats: GamificationStats): GamificationInfo {
    var status = Status.UNKNOWN_ERROR
    if (levels.status == Levels.Status.OK && gamificationStats.status == GamificationStats.Status.OK) {
      return GamificationInfo(gamificationStats.level, gamificationStats.totalSpend,
          gamificationStats.nextLevelAmount, levels.list, levels.updateDate,
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
      val levels = map(gamification.levels, currentLevel)
      view.displayGamificationInfo(currentLevel, gamification.nextLevelAmount, levels.first,
          levels.second, gamification.totalSpend, gamification.updateDate)
      if (sendEvent) analytics.sendMainScreenViewEvent(currentLevel + 1)
    }
  }

  private fun map(levels: List<Levels.Level>,
                  currentLevel: Int): Pair<List<LevelModel>, List<LevelModel>> {
    val hiddenList = ArrayList<LevelModel>()
    val shownList = ArrayList<LevelModel>()
    for (level in levels) {
      val viewType = when {
        level.level < currentLevel -> LevelType.REACHED
        level.level == currentLevel -> LevelType.CURRENT
        else -> LevelType.UNREACHED
      }
      val levelViewModel = LevelModel(level.amount, level.bonus, level.level, viewType)
      if (viewType == LevelType.REACHED) hiddenList.add(levelViewModel)
      else shownList.add(levelViewModel)
    }
    return Pair(hiddenList, shownList)
  }

  private fun handleHeaderInformation(totalEarned: BigDecimal, totalSpend: BigDecimal,
                                      status: GamificationStats.Status) {
    if (status == GamificationStats.Status.OK) {
      disposables.add(gamification.getAppcToLocalFiat(totalEarned.toString(), 2)
          .filter { it.amount.toInt() >= 0 }
          .observeOn(viewScheduler)
          .doOnNext {
            val totalSpent = formatter.formatCurrency(totalSpend, WalletCurrency.FIAT)
            val bonusEarned = formatter.formatCurrency(it.amount, WalletCurrency.FIAT)
            view.showHeaderInformation(totalSpent, bonusEarned, it.symbol)
          }
          .subscribeOn(networkScheduler)
          .subscribe({}, { handleError(it) }))
    }
  }

  private fun handleError(throwable: Throwable) {
    throwable.printStackTrace()
    if (throwable.isNoNetworkException()) {
      activityView.showNetworkErrorView()
    }
  }

  fun stop() = disposables.clear()

  private fun handleBottomSheetVisibility() {
    disposables.add(view.getBottomSheetButtonClick()
        .mergeWith(view.getBottomSheetContainerClick())
        .observeOn(viewScheduler)
        .doOnNext { view.updateBottomSheetVisibility() }
        .subscribe({}, { handleError(it) }))
  }

  private fun handleBackPress() {
    disposables.add(Observable.merge(view.getBackPressed(), view.getHomeBackPressed())
        .observeOn(viewScheduler)
        .doOnNext { view.handleBackPressed() }
        .subscribe({}, { it.printStackTrace() }))
  }
}
