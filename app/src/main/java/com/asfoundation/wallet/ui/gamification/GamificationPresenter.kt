package com.asfoundation.wallet.ui.gamification

import android.os.Bundle
import com.appcoins.wallet.core.analytics.analytics.gamification.GamificationAnalytics
import com.appcoins.wallet.core.utils.android_common.CurrencyFormatUtils
import com.appcoins.wallet.core.utils.android_common.WalletCurrency
import com.appcoins.wallet.core.utils.android_common.extensions.isNoNetworkException
import com.appcoins.wallet.feature.changecurrency.data.currencies.FiatValue
import com.appcoins.wallet.feature.changecurrency.data.use_cases.GetCachedCurrencySymbolUseCase
import com.appcoins.wallet.feature.changecurrency.data.use_cases.GetCachedCurrencyUseCase
import com.appcoins.wallet.gamification.GamificationContext
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.PromotionsGamificationStats
import com.asfoundation.wallet.ui.gamification.GamificationFragment.Companion.GAMIFICATION_INFO_ID
import com.asfoundation.wallet.ui.gamification.GamificationFragment.Companion.SHOW_REACHED_LEVELS_ID
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import java.math.BigDecimal

class GamificationPresenter(
  private val view: GamificationView,
  private val activityView: GamificationActivityView,
  private val gamification: GamificationInteractor,
  private val analytics: GamificationAnalytics,
  private val formatter: CurrencyFormatUtils,
  private val disposables: CompositeDisposable,
  private val viewScheduler: Scheduler,
  private val networkScheduler: Scheduler,
  private val getCachedCurrencySymbolUseCase: GetCachedCurrencySymbolUseCase,
  private val getCachedCurrencyUseCase: GetCachedCurrencyUseCase
) {

  private var viewHasContent = false

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
      Observable.zip(gamification.getLevels(), gamification.getUserStats(),
        BiFunction { levels: Levels, promotionsGamificationStats: PromotionsGamificationStats ->
          mapToGamificationInfo(
            levels,
            promotionsGamificationStats
          )
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
        .subscribe({}, { handleError(it) })
    )
  }

  private fun mapToGamificationInfo(
    levels: Levels, promotionsGamificationStats: PromotionsGamificationStats
  ): GamificationInfo {
    var status = Status.UNKNOWN_ERROR
    if (levels.status == Levels.Status.OK && promotionsGamificationStats.resultState == PromotionsGamificationStats.ResultState.OK) {
      return GamificationInfo(
        promotionsGamificationStats.level, promotionsGamificationStats.totalSpend,
        if (promotionsGamificationStats.totalEarned >= BigDecimal.ZERO) getCachedCurrencyUseCase()?.let { currency ->
          getCachedCurrencySymbolUseCase()?.let { symbol ->
            FiatValue(promotionsGamificationStats.totalEarned,
              currency, symbol
            )
          }
        } else null,
        promotionsGamificationStats.nextLevelAmount, levels.list, levels.updateDate, Status.OK,
        promotionsGamificationStats.fromCache
      )
    }
    if (levels.status == Levels.Status.NO_NETWORK || promotionsGamificationStats.resultState == PromotionsGamificationStats.ResultState.NO_NETWORK) {
      status = Status.NO_NETWORK
    }
    return GamificationInfo(status, promotionsGamificationStats.fromCache)
  }

  private fun displayInformation(gamification: GamificationInfo) {
    if (gamification.status != Status.OK) {
      if (!viewHasContent && !gamification.fromCache) activityView.showNetworkErrorView()
    } else {
      activityView.showMainView()
      displayHeaderInformation(gamification)
      val levels = mapToLevelsList(gamification)
      view.displayGamificationInfo(levels.first, levels.second, gamification.updateDate)
      viewHasContent = true
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

  private fun mapToLevelsList(
    gamification: GamificationInfo
  ): Pair<List<LevelItem>, List<LevelItem>> {
    val hiddenList = ArrayList<LevelItem>()
    val shownList = ArrayList<LevelItem>()
    val currentLevel = gamification.currentLevel
    for (level in gamification.levels) {
      val levelItem = when {
        level.level < currentLevel -> ReachedLevelItem(level.amount, level.bonus, level.level, getCachedCurrencySymbolUseCase())
        level.level == currentLevel -> CurrentLevelItem(
          level.amount, level.bonus, level.level,
          gamification.totalSpend, gamification.nextLevelAmount, getCachedCurrencySymbolUseCase()
        )

        else -> UnreachedLevelItem(level.amount, level.bonus, level.level, getCachedCurrencySymbolUseCase())
      }
      if (levelItem is ReachedLevelItem) hiddenList.add(levelItem)
      else shownList.add(levelItem)
    }
    return Pair(hiddenList, shownList)
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
      .subscribe({}, { handleError(it) })
    )
  }

  private fun handleBackPress() {
    disposables.add(Observable.merge(view.getBackPressed(), view.getHomeBackPressed())
      .observeOn(viewScheduler)
      .doOnNext { view.handleBackPressed() }
      .subscribe({}, { it.printStackTrace() })
    )
  }
}
