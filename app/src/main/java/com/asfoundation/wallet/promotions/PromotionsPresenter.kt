package com.asfoundation.wallet.promotions

import com.appcoins.wallet.gamification.GamificationScreen
import com.appcoins.wallet.gamification.repository.Levels
import com.appcoins.wallet.gamification.repository.UserStats
import com.asfoundation.wallet.promotions.PromotionsInteractorContract.PromotionType
import com.asfoundation.wallet.referrals.ReferralsScreen
import com.asfoundation.wallet.ui.gamification.GamificationInteractor
import com.asfoundation.wallet.ui.gamification.UserRewardsStatus
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function3
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.TimeUnit

class PromotionsPresenter(private val view: PromotionsView,
                          private val gamification: GamificationInteractor,
                          private val promotionsInteractor: PromotionsInteractorContract,
                          private val disposables: CompositeDisposable,
                          private val networkScheduler: Scheduler,
                          private val viewScheduler: Scheduler) {

  fun present() {
    retrievePromotions()
    retrieveReferralBonus()
    handleGamificationNavigationClicks()
    handleDetailsClick()
    handleShareClick()
    handleRetryClick()
    view.setupLayout()
  }

  private fun retrievePromotions() {
    disposables.add(
        promotionsInteractor.retrievePromotions().toObservable()
            .flatMapIterable { it }
            .observeOn(viewScheduler)
            .doOnNext { showPromotions(it) }
            .subscribe({}, { handlerError(it) }))
  }

  private fun retrieveReferralBonus() {
    disposables.add(promotionsInteractor.retrieveReferralBonus()
        .observeOn(viewScheduler)
        .doOnSuccess { view.setReferralBonus(it) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleNewReferralUpdate() {
    disposables.add(promotionsInteractor.hasReferralUpdate(ReferralsScreen.REFERRAL)
        .observeOn(viewScheduler)
        .doOnSuccess { view.showReferralUpdate(it) }
        .flatMapCompletable {
          promotionsInteractor.saveReferralInformation(
              ReferralsScreen.PROMOTIONS)
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleNewLevel() {
    disposables.add(gamification.hasNewLevel(GamificationScreen.MY_LEVEL)
        .observeOn(viewScheduler)
        .doOnSuccess { view.showGamificationUpdate(it) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleShareClick() {
    disposables.add(view.shareClick()
        .doOnNext { view.showShare() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleDetailsClick() {
    disposables.add(
        Observable.merge(view.detailsClick(), view.referralCardClick())
            .doOnNext { view.detailsClick() }
            .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleGamificationNavigationClicks() {
    disposables.add(Observable.merge(view.seeMoreClick(), view.gamificationCardClick())
        .doOnNext { view.navigateToGamification() }
        .subscribe({}, { it.printStackTrace() }))
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

  private fun showPromotions(promotion: PromotionType) {
    when (promotion) {
      PromotionType.REFERRAL -> {
        view.showReferralCard()
        handleNewReferralUpdate()
      }
      PromotionType.GAMIFICATION -> {
        view.showGamificationCard()
        handleShowLevels()
        handleNewLevel()
      }
    }
  }

  private fun handlerError(throwable: Throwable) {
    throwable.printStackTrace()
    if (isNoNetworkException(throwable)) {
      view.showNetworkErrorView()
    }
  }

  private fun isNoNetworkException(throwable: Throwable): Boolean {
    return throwable is IOException || throwable.cause != null && throwable.cause is IOException
  }

  private fun handleRetryClick() {
    disposables.add(view.retryClick()
        .observeOn(viewScheduler)
        .flatMapCompletable {
          Completable.fromAction { view.showRetryAnimation() }
              .andThen(Completable.timer(1, TimeUnit.SECONDS))
              .andThen { retrievePromotions() }
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() {
    disposables.clear()
  }

}
