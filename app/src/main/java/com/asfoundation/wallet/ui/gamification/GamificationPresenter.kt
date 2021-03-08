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
import com.asfoundation.wallet.ui.iab.FiatValue
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.util.isNoNetworkException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
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
    disposables.add(
        Observable.zip(gamification.getLevels(), getUserStatsAndBonusEarned(),
            BiFunction { levels: Levels, statsAndBonusEarned: Pair<GamificationStats, FiatValue> ->
              mapToGamificationInfo(levels, statsAndBonusEarned.first, statsAndBonusEarned.second)
            })
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnNext { displayInformation(it) }
            .observeOn(networkScheduler)
            .filter { it.status == Status.OK }
            .map { it.currentLevel }
            .lastOrError()
            .onErrorReturn { -1 }
            .flatMapCompletable {
              gamification.levelShown(it, GamificationContext.SCREEN_MY_LEVEL)
                  .andThen(Completable.fromAction {
                    if (sendEvent) analytics.sendMainScreenViewEvent(it + 1)
                  })
            }
            .subscribe({}, { handleError(it) }))
  }

  private fun mapToGamificationInfo(levels: Levels, gamificationStats: GamificationStats,
                                    bonusEarned: FiatValue): GamificationInfo {
    var status = Status.UNKNOWN_ERROR
    if (levels.status == Levels.Status.OK && gamificationStats.status == GamificationStats.Status.OK) {
      return GamificationInfo(gamificationStats.level, gamificationStats.totalSpend,
          if (bonusEarned.amount >= BigDecimal.ZERO) bonusEarned else null,
          gamificationStats.nextLevelAmount, levels.list, levels.updateDate, Status.OK,
          gamificationStats.fromCache)
    }
    if (levels.status == Levels.Status.NO_NETWORK || gamificationStats.status == GamificationStats.Status.NO_NETWORK) {
      status = Status.NO_NETWORK
    }
    return GamificationInfo(status, gamificationStats.fromCache)
  }

  private fun displayInformation(gamification: GamificationInfo) {
    if (gamification.status != Status.OK) {
      // this may flicker between something displayed that is in cache and then a no network view.
      // Since there is info that must be retrieved from API, and only from API, it's best to
      // notify the user that there is no network than the user thinking new data will eventually come
      if (!gamification.fromCache) {
        activityView.showNetworkErrorView()
      }
    } else {
      val currentLevel = gamification.currentLevel
      activityView.showMainView()
      displayHeaderInformation(gamification)
      val levels = map(gamification.levels, currentLevel)
      view.displayGamificationInfo(currentLevel, gamification.nextLevelAmount, levels.first,
          levels.second, gamification.totalSpend, gamification.updateDate)
    }
  }

  private fun displayHeaderInformation(gamification: GamificationInfo) {
    if (gamification.totalEarned != null) {
      val totalSpent = formatter.formatCurrency(gamification.totalSpend, WalletCurrency.FIAT)
      val bonusEarned =
          formatter.formatCurrency(gamification.totalEarned.amount, WalletCurrency.FIAT)
      view.showHeaderInformation(totalSpent, bonusEarned, gamification.totalEarned.symbol)
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

  private fun getUserStatsAndBonusEarned(): Observable<Pair<GamificationStats, FiatValue>> {
    return gamification.getUserStats()
        .flatMap { stats ->
          if (stats.status == GamificationStats.Status.OK && !stats.fromCache) {
            gamification.getAppcToLocalFiat(stats.totalEarned.toString(), 2)
                .map { Pair(stats, it) }
          } else {
            Observable.just(Pair(stats, FiatValue(BigDecimal.ONE.negate(), "")))
          }
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
