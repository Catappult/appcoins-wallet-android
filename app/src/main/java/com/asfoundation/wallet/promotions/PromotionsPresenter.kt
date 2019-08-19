package com.asfoundation.wallet.promotions

import com.appcoins.wallet.gamification.GamificationScreen
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.UserStats
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.gamification.UserRewardsStatus
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import java.math.BigDecimal
import java.math.RoundingMode

class PromotionsPresenter(private val view: PromotionsView,
                          private val gamification: GamificationInteractor,
                          private val disposables: CompositeDisposable,
                          private val networkScheduler: Scheduler,
                          private val viewScheduler: Scheduler) {

  fun present() {
    handleShowLevels()
    handleGamificationNavigationClicks()
    handleDetailsClick()
    handleShareClick()
    view.setupLayout()
  }

  private fun handleShareClick() {
    disposables.add(
        view.shareClick().doOnNext { view.showShare() }.subscribe({}, { it.printStackTrace() }))
  }

  private fun handleDetailsClick() {
    disposables.add(
        Observable.merge(view.detailsClick(),
            view.referralCardClick()).doOnNext { view.detailsClick() }.subscribe({},
            { it.printStackTrace() }))
  }

  private fun handleGamificationNavigationClicks() {
    disposables.add(Observable.merge(view.seeMoreClick(),
        view.gamificationCardClick()).doOnNext { view.navigateToGamification() }.subscribe({},
        { it.printStackTrace() }))
  }

  private fun handleShowLevels() {
    disposables.add(
        Single.zip(gamification.getLevels(), gamification.getUserStatus(),
            gamification.getLastShownLevel(GamificationScreen.PROMOTIONS),
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
            }
            .flatMapCompletable { gamification.levelShown(it.level, GamificationScreen.PROMOTIONS) }
            .subscribe({}, { it.printStackTrace() }))
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
    return UserRewardsStatus(lastShownLevel, lastShownLevel)
  }

}
