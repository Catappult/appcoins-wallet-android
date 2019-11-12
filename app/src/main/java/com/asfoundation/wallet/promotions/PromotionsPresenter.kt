package com.asfoundation.wallet.promotions

import com.appcoins.wallet.gamification.GamificationScreen
import com.asfoundation.wallet.referrals.ReferralsScreen
import com.asfoundation.wallet.ui.gamification.Status
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.io.IOException
import java.util.concurrent.TimeUnit

class PromotionsPresenter(private val view: PromotionsView,
                          private val promotionsInteractor: PromotionsInteractorContract,
                          private val disposables: CompositeDisposable,
                          private val networkScheduler: Scheduler,
                          private val viewScheduler: Scheduler) {

  fun present() {
    retrievePromotions()
    handleGamificationNavigationClicks()
    handleDetailsClick()
    handleShareClick()
    handleRetryClick()
    handleShowLevels()
    view.setupLayout()
  }

  private fun retrievePromotions() {
    disposables.add(
        promotionsInteractor.retrievePromotions()
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess {
              view.setReferralBonus(it.maxValue, it.currency)
              view.toggleShareAvailability(it.isValidated)
              showPromotions(it)
            }
            .subscribe({}, { handleError(it) }))
  }

  private fun handleNewLevel() {
    disposables.add(
        promotionsInteractor.hasGamificationNewLevel(GamificationScreen.MY_LEVEL)
            .observeOn(viewScheduler)
            .doOnSuccess { view.showGamificationUpdate(it) }
            .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleShareClick() {
    disposables.add(view.shareClick()
        .observeOn(networkScheduler)
        .flatMapSingle { promotionsInteractor.retrievePromotions() }
        .observeOn(viewScheduler)
        .doOnNext { view.showShare(it.link!!) }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleDetailsClick() {
    disposables.add(
        Observable.merge(view.detailsClick(), view.referralCardClick())
            .doOnNext { view.navigateToInviteFriends() }
            .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleGamificationNavigationClicks() {
    disposables.add(Observable.merge(view.seeMoreClick(), view.gamificationCardClick())
        .doOnNext { view.navigateToGamification() }
        .subscribe({}, { it.printStackTrace() }))
  }

  private fun handleShowLevels() {
    disposables.add(
        promotionsInteractor.retrieveGamificationRewardStatus(GamificationScreen.PROMOTIONS)
            .subscribeOn(networkScheduler)
            .observeOn(viewScheduler)
            .doOnSuccess {
              if (it.status == Status.NO_NETWORK) {
                view.showNetworkErrorView()
              } else {
                if (it.lastShownLevel > 0 || it.lastShownLevel == 0 && it.level == 0) {
                  view.setStaringLevel(it)
                }
                view.updateLevel(it)
              }
            }
            .flatMapCompletable {
              promotionsInteractor.levelShown(it.level, GamificationScreen.PROMOTIONS)
            }
            .subscribe({}, { handleError(it) }))
  }


  private fun showPromotions(promotionsModel: PromotionsModel) {
    view.hideLoading()
    if (promotionsModel.referralsAvailable) {
      view.showReferralCard()
      checkForReferralsUpdates(promotionsModel)
    }
    if (promotionsModel.gamificationAvailable) {
      view.showGamificationCard()
      handleNewLevel()
    }
  }

  private fun checkForReferralsUpdates(promotionsModel: PromotionsModel) {
    disposables.add(promotionsInteractor.hasReferralUpdate(promotionsModel.numberOfInvitations,
        promotionsModel.isValidated, ReferralsScreen.INVITE_FRIENDS)
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { view.showReferralUpdate(it) }
        .flatMapCompletable {
          promotionsInteractor.saveReferralInformation(promotionsModel.numberOfInvitations,
              promotionsModel.isValidated, ReferralsScreen.PROMOTIONS)
        }
        .subscribeOn(networkScheduler)
        .subscribe({}, { handleError(it) }))
  }


  private fun handleError(throwable: Throwable) {
    throwable.printStackTrace()
    if (isNoNetworkException(throwable)) {
      view.hideLoading()
      view.showNetworkErrorView()
    }
  }

  private fun isNoNetworkException(throwable: Throwable): Boolean {
    return throwable is IOException || throwable.cause != null && throwable.cause is IOException
  }

  private fun handleRetryClick() {
    disposables.add(view.retryClick()
        .observeOn(viewScheduler)
        .doOnNext { view.showRetryAnimation() }
        .delay(1, TimeUnit.SECONDS)
        .doOnNext {
          retrievePromotions()
          handleShowLevels()
        }
        .subscribe({}, { it.printStackTrace() }))
  }

  fun stop() {
    disposables.clear()
  }

}
