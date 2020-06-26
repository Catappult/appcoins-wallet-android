package com.asfoundation.wallet.promotions

import com.appcoins.wallet.gamification.GamificationScreen
import com.appcoins.wallet.gamification.repository.UserType
import com.asfoundation.wallet.referrals.ReferralsScreen
import com.asfoundation.wallet.ui.gamification.Status
import com.asfoundation.wallet.util.CurrencyFormatUtils
import com.asfoundation.wallet.util.WalletCurrency
import com.asfoundation.wallet.util.isNoNetworkException
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit

class PromotionsPresenter(private val view: PromotionsView,
                          private val activityView: PromotionsActivityView,
                          private val promotionsInteractor: PromotionsInteractorContract,
                          private val disposables: CompositeDisposable,
                          private val networkScheduler: Scheduler,
                          private val viewScheduler: Scheduler,
                          private val formatter: CurrencyFormatUtils) {

  var cachedUserType: UserType = UserType.STANDARD
  var cachedLink: String = ""

  fun present() {
    retrievePromotions()
    handleGamificationNavigationClicks()
    handleDetailsClick()
    handleShareClick()
    handleRetryClick()
  }

  private fun retrievePromotions() {
    disposables.add(promotionsInteractor.retrievePromotions()
        .subscribeOn(networkScheduler)
        .observeOn(viewScheduler)
        .doOnSuccess { onPromotions(it) }
        .subscribe({}, { handleError(it) }))
  }

  private fun onPromotions(promotionsModel: PromotionsModel) {
    view.hideLoading()
    if (promotionsModel.gamificationAvailable || promotionsModel.referralsAvailable) {
      showPromotions(promotionsModel)
    } else {
      view.showNoPromotionsScreen()
    }
  }

  private fun handleNewLevel() {
    disposables.add(promotionsInteractor.hasGamificationNewLevel(GamificationScreen.MY_LEVEL)
        .observeOn(viewScheduler)
        .doOnSuccess { view.showGamificationUpdate(it) }
        .subscribe({}, { handleError(it) }))
  }

  private fun handleShareClick() {
    disposables.add(view.shareClick()
        .doOnNext { activityView.handleShare(cachedLink) }
        .subscribe({}, { handleError(it) }))
  }

  private fun handleDetailsClick() {
    disposables.add(Observable.merge(view.detailsClick(), view.referralCardClick())
        .doOnNext { activityView.navigateToInviteFriends() }
        .subscribe({}, { handleError(it) }))
  }

  private fun handleGamificationNavigationClicks() {
    disposables.add(Observable.merge(view.seeMoreClick(), view.gamificationCardClick())
        .doOnNext {
          if (isLegacyUser(cachedUserType)) activityView.navigateToLegacyGamification()
          else activityView.navigateToGamification()
        }
        .subscribe({}, { handleError(it) }))
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
    if (promotionsModel.referralsAvailable) {
      cachedLink = promotionsModel.link
      view.setReferralBonus(formatter.formatCurrency(promotionsModel.maxValue, WalletCurrency.FIAT),
          promotionsModel.currency)
      view.toggleShareAvailability(promotionsModel.isValidated)
      view.showReferralCard()
      checkForReferralsUpdates(promotionsModel)
    }
    if (promotionsModel.gamificationAvailable) {
      cachedUserType = promotionsModel.userType
      view.setLevelIcons()
      view.showGamificationCard()
      handleShowLevels()
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
    view.hideLoading()
    if (throwable.isNoNetworkException()) view.showNetworkErrorView()
  }

  private fun handleRetryClick() {
    disposables.add(view.retryClick()
        .observeOn(viewScheduler)
        .doOnNext { view.showRetryAnimation() }
        .delay(1, TimeUnit.SECONDS)
        .doOnNext { retrievePromotions() }
        .subscribe({}, { handleError(it) }))
  }

  private fun isLegacyUser(userType: UserType): Boolean {
    return userType == UserType.PIONEER || userType == UserType.INNOVATOR
  }

  fun stop() = disposables.clear()

}
